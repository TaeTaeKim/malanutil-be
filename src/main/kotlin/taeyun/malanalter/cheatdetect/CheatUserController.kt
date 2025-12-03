package taeyun.malanalter.cheatdetect

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.cheatdetect.dto.CheatSearchResult

@RestController
class CheatUserController(
    private val cheatSearchService: CheatSearchService
) {

    @GetMapping("/cheat")
    fun searchCheatHistory(@RequestParam characterName : String) : CheatSearchResult {
        return cheatSearchService.searchWithUserName(characterName)

    }
}