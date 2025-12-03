package taeyun.malanalter.cheatdetect.cheatagent

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactive.awaitSingle
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import taeyun.malanalter.cheatdetect.CheatSearchDomain
import taeyun.malanalter.cheatdetect.dto.CheatArticle
import java.time.ZonedDateTime

private val logger = KotlinLogging.logger {}
@Component
class MalanTalkCheatAgent(
    private val webClient: WebClient = WebClient.builder().build(),
    override val domain: CheatSearchDomain = CheatSearchDomain.MALANTALK
) : CheatSearchAgent {
    override suspend fun searchWithUsername(username: String): CheatArticle {
        return try{
            val res = fetchJson(username)
            CheatArticle.fromMalanTalkRes(res)
        }catch (e : Exception){
            logger.error(e) { "Failed to search cheat articles from DC Inside for username: $username" }
            CheatArticle.makeEmpty(domain)
        }
    }

    private suspend fun fetchJson(username: String): MalantalkResponse {
        return webClient.get()
            .uri { builder ->
                builder.scheme("https")
                    .host("malan.onrender.com")
                    .path("/api/accident")
                    .queryParam("title", username)
                    .build()
            }.retrieve()
            .bodyToMono(MalantalkResponse::class.java)
            .awaitSingle()
    }

}

data class MalantalkResponse(
    val posts: List<MalanTalkPost>,
    val totalCount: Int
)
data class MalanTalkPost(
    val _id: String,
    val title: String,
    val content: String,
    val reporter: String,
    val target: String,
    val timestamp: ZonedDateTime
)