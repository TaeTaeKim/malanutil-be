package taeyun.malanalter.cheatdetect

import org.springframework.stereotype.Service
import taeyun.malanalter.cheatdetect.cheatagent.CheatSearchAgent
import taeyun.malanalter.cheatdetect.dto.CheatArticle

@Service
class CheatSearchService (
    private val agents: List<CheatSearchAgent>
){
    fun searchWithUserName(characterName: String): List<CheatArticle> {
        TODO("Not yet implemented")
    }

}