package taeyun.malanalter.cheatdetect.cheatagent

import taeyun.malanalter.cheatdetect.CheatSearchDomain
import taeyun.malanalter.cheatdetect.dto.CheatArticle

interface CheatSearchAgent {
    val domain: CheatSearchDomain
    suspend fun searchWithUsername(username: String) : CheatArticle
}