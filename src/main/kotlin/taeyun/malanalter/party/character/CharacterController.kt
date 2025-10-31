package taeyun.malanalter.party.character

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.character.dto.CharacterDto

// 메랜팟 유저들의 캐릭터 정보를 관리하는 컨트롤러
@RequestMapping("/character")
@RestController
class CharacterController(val characterService: CharacterService) {

    @GetMapping
    fun getAllCharacters(): List<CharacterDto> {
        return characterService.fetchAllUserCharacters()
    }

    @PatchMapping("/{characterId}")
    fun updateCharacter(@PathVariable characterId: String,@RequestBody characterDto: CharacterDto): CharacterDto {
        return characterService.updateCharacter(characterId, characterDto)
    }

    @DeleteMapping("/{characterId}")
    fun deleteCharacter(@PathVariable characterId: String) {
        characterService.deleteCharacter(characterId)
    }
    @PostMapping
    fun createCharacter(@RequestBody characterDto: CharacterDto): CharacterDto {
        return characterService.createCharacter(characterDto)
    }

}