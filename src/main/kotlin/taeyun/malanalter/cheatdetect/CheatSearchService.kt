package taeyun.malanalter.cheatdetect

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.springframework.stereotype.Service
import taeyun.malanalter.cheatdetect.cheatagent.CheatSearchAgent
import taeyun.malanalter.cheatdetect.dto.CheatArticle

@Service
class CheatSearchService (
    private val agents: List<CheatSearchAgent>
){
    suspend fun searchAllDomains(characterName: String): List<CheatArticle> {
        val deferedResults = agents.map{ agent ->
            coroutineScope {
                async {
                    try {
                        agent.searchWithUsername(characterName)
                    }catch (e : Exception){
                        CheatArticle.makeEmpty(agent.domain)
                    }
                }
            }
        }
        return deferedResults.awaitAll()
    }
}