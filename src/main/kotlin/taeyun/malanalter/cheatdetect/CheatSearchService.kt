package taeyun.malanalter.cheatdetect

import org.springframework.stereotype.Service
import taeyun.malanalter.cheatdetect.cheatagent.CheatSearchAgent
import taeyun.malanalter.cheatdetect.dto.CheatSearchResult

@Service
class CheatSearchService (
    private val agents: List<CheatSearchAgent>
){
    fun searchWithUserName(characterName: String): CheatSearchResult {
        TODO("Not yet implemented")
    }

}