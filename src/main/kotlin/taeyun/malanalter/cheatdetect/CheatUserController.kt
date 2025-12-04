package taeyun.malanalter.cheatdetect

import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.cheatdetect.dto.CheatArticle
import taeyun.malanalter.config.CacheService

@RestController
class CheatUserController(
    private val cheatSearchService: CheatSearchService,
    private val cacheService: CacheService,
) {

    @GetMapping("/cheat")
    fun searchCheatHistory(@RequestParam characterName: String): List<CheatArticle> =
        cacheService.cacheable("cheat_user", characterName) {
            runBlocking {
                cheatSearchService.searchAllDomains(characterName)
            }
        }
}