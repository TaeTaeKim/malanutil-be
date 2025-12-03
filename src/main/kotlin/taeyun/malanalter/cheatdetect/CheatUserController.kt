package taeyun.malanalter.cheatdetect

import kotlinx.coroutines.runBlocking
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.cheatdetect.dto.CheatArticle

@RestController
class CheatUserController(
    private val cheatSearchService: CheatSearchService
) {

    @GetMapping("/cheat")
    fun searchCheatHistory(@RequestParam characterName : String) : List<CheatArticle> = runBlocking {
        println("=== Request received for: $characterName ===")
        val result = cheatSearchService.searchAllDomains(characterName)
        println("=== Result count: ${result.size} ===")
        result
    }
}