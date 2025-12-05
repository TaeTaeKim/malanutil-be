package taeyun.malanalter.cheatdetect.cheatagent

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.jsoup.Jsoup
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import taeyun.malanalter.cheatdetect.CheatSearchDomain
import taeyun.malanalter.cheatdetect.dto.CheatArticle

private val logger = KotlinLogging.logger {}

@Component
class MatipCheatAgent(
    private val webClient: WebClient = WebClient.builder().build(), // WebClient for async HTTP requests
    override val domain: CheatSearchDomain = CheatSearchDomain.MATIP
) : CheatSearchAgent {

    companion object {
        private const val BASE_URL = "https://matip.kr/cheat/search"
        // CSS selector for the table body - using .table class which is unique
        private const val TABLE_SELECTOR = "table.table tbody"
    }

    override suspend fun searchWithUsername(username: String): CheatArticle {
        return try {
            // Step 1: Fetch HTML from Matip website
            val html = fetchHtml(username)

            // Step 2: Count results and create article with search URL
            val searchUrl = "$BASE_URL?q=$username"
            parseArticles(html, searchUrl)
        } catch (e: Exception) {
            logger.error(e) { "Failed to search cheat articles from Matip for username: $username" }
            CheatArticle.makeEmpty(domain)
        }
    }

    /**
     * Fetches HTML content from Matip search page
     */
    private suspend fun fetchHtml(username: String): String {
        return webClient.get()
            .uri { builder ->
                builder.scheme("https")
                    .host("matip.kr")
                    .path("/cheat/search")
                    .queryParam("q", username) // Add username as query parameter
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle() // Convert Mono to suspend function
    }

    /**
     * Parses HTML and counts the number of result rows
     * Returns a single CheatArticle with the count and search URL
     */
    private fun parseArticles(html: String, searchUrl: String): CheatArticle {
        val document = Jsoup.parse(html)
        val tbody = document.selectFirst(TABLE_SELECTOR) ?: run {
            logger.warn { "Table body not found in HTML response" }
            return CheatArticle.makeEmpty(domain)
        }

        // Count the number of table rows, filtering out "no results" row
        // The "no results" row has: <td colspan="6" class="has-text-centered">해당하는 사례가 없습니다</td>
        val validRows = tbody.select("tr").filter { row ->
            val td = row.selectFirst("td")
            // Exclude rows with colspan (indicates "no results" message)
            td != null && !td.hasAttr("colspan")
        }

        val rowCount = validRows.size

        // Return a single article with the count and search URL
        return if (rowCount > 0) {
            CheatArticle(
                domain = domain,
                count = rowCount,
                link = searchUrl
            )
        } else {
            CheatArticle.makeEmpty(domain)
        }
    }
}