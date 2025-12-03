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
class DcinsideCheatAgent(
    private val webClient: WebClient = WebClient.builder().build(), // WebClient for async HTTP requests
    override val domain: CheatSearchDomain = CheatSearchDomain.DCINSIDE
) : CheatSearchAgent {

    companion object {
        private const val BASE_URL = "https://gall.dcinside.com/mini/board/lists/"
        private const val GALLERY_ID = "mswbz" // DC Inside gallery ID
        private const val SEARCH_TYPE = "search_subject_memo" // Search in subject and memo

        // CSS selector for the list wrapper containing search results
        private const val LIST_WRAPPER_SELECTOR = ".listwrap2"
        private const val SUBJECT_SELECTOR = ".gall_subject" // Subject column selector

        // Text to filter out
        private val EXCLUDED_TEXTS = setOf("설문", "AD") // Poll and Advertisement
    }

    override suspend fun searchWithUsername(username: String): CheatArticle {
        return try {
            // Step 1: Fetch HTML from DC Inside
            logger.info { "Searching for $username for $domain" }
            val html = fetchHtml(username)

            // Step 2: Count valid results and create article with search URL
            val searchUrl = "$BASE_URL?id=$GALLERY_ID&s_type=$SEARCH_TYPE&s_keyword=$username"
            parseArticles(html, searchUrl)
        } catch (e: Exception) {
            logger.error(e) { "Failed to search cheat articles from DC Inside for username: $username" }
            CheatArticle.makeEmpty(domain)
        }
    }

    /**
     * Fetches HTML content from DC Inside search page
     */
    private suspend fun fetchHtml(username: String): String {
        return webClient.get()
            .uri { builder ->
                builder.scheme("https")
                    .host("gall.dcinside.com")
                    .path("/mini/board/lists/")
                    .queryParam("id", GALLERY_ID) // Gallery ID
                    .queryParam("s_type", SEARCH_TYPE) // Search type
                    .queryParam("s_keyword", username) // Search keyword (username)
                    .build()
            }
            .retrieve()
            .bodyToMono(String::class.java)
            .awaitSingle() // Convert Mono to suspend function
    }

    /**
     * Parses HTML and counts valid gall_subject elements
     * Filters out "설문" (poll) and "AD" (advertisement) entries
     */
    private fun parseArticles(html: String, searchUrl: String): CheatArticle {
        val document = Jsoup.parse(html)
        val listWrapper = document.selectFirst(LIST_WRAPPER_SELECTOR) ?: run {
            logger.warn { "List wrapper not found in HTML response" }
            return CheatArticle.makeEmpty(domain)
        }

        // Select all .gall_subject elements
        val subjects = listWrapper.select(SUBJECT_SELECTOR)

        // Count only subjects that are NOT "설문" or "AD"
        val validCount = subjects.count { element ->
            val text = element.text().trim()
            text !in EXCLUDED_TEXTS // Exclude "설문" and "AD"
        }

        // Return a single article with the count and search URL
        return if (validCount > 0) {
                CheatArticle(
                    domain = domain,
                    count = validCount,
                    link = searchUrl
                )
        } else {
            CheatArticle.makeEmpty(domain)
        }
    }
}