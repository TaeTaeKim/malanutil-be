package taeyun.malanalter.cheatdetect

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import taeyun.malanalter.cheatdetect.cheatagent.CheatSearchAgent
import taeyun.malanalter.cheatdetect.dto.CheatArticle
private val logger = KotlinLogging.logger {}
@Service
class CheatSearchService (
    private val agents: List<CheatSearchAgent>
){
    suspend fun searchAllDomains(characterName: String): List<CheatArticle> = coroutineScope {
        // Create all async tasks within the same coroutineScope for parallel execution
        agents.map { agent ->
            async {
                try {
                    agent.searchWithUsername(characterName)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to search for $characterName in ${agent.domain}" }
                    CheatArticle.makeEmpty(agent.domain)
                }
            }
        }.awaitAll()
    }
}