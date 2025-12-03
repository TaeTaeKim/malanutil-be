package taeyun.malanalter.cheatdetect.cheatagent

import org.springframework.stereotype.Component
import taeyun.malanalter.cheatdetect.CheatSearchDomain
import taeyun.malanalter.cheatdetect.dto.CheatArticle

@Component
class MatipCheatAgent(override val domain: CheatSearchDomain= CheatSearchDomain.MATIP) : CheatSearchAgent {
    override suspend fun searchWithUsername(username: String): List<CheatArticle> {
        TODO("Not yet implemented")
    }
}