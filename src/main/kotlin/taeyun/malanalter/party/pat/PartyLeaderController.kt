package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.pat.dto.PartyCreate
import taeyun.malanalter.party.pat.dto.PartyResponse

@RestController
@RequestMapping("/party/leader")
class PartyLeaderController(
    val partyLeaderService: PartyLeaderService
) {

    // 파티생성 컨트롤러
    @PostMapping("/{mapId}")
    fun createParty(@PathVariable mapId: Long, @RequestBody party: PartyCreate) : PartyResponse {
        return partyLeaderService.createParty(mapId, party)
    }

    @GetMapping
    fun getLeaderParty(): PartyResponse? {
        return partyLeaderService.getLeaderParty()
    }

    @GetMapping("/history/{mapId}")
    fun getPartyCreationHistory(@PathVariable mapId: Long): PartyCreate? {
        return partyLeaderService.getPartyCreationHistory(mapId)
    }


    @DeleteMapping("/{partyId}")
    fun deleteParty(@PathVariable partyId: String) {
        partyLeaderService.deleteParty(partyId)
    }

    @PostMapping("/invite/{userId}")
    fun inviteUserToParty(@PathVariable userId: Long) {

    }
    @PostMapping("/apply/{applyId}")
    fun handlePartyApplication(@PathVariable applyId: Long) {

    }

    @GetMapping("/talent/{mapId}")
    fun getTalentPool(@PathVariable mapId: Long) {

    }

}