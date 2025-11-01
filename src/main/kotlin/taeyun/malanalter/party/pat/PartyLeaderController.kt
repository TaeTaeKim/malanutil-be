package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.pat.dto.PartyCreate
import taeyun.malanalter.party.pat.dto.PartyResponse

@RestController
@RequestMapping("/party/leader")
class PartyLeaderController(
    val partyService: PartyService
) {

    // 파티생성 컨트롤러
    @PostMapping("/{mapId}")
    fun createParty(@PathVariable mapId: Long, @RequestBody party: PartyCreate) : PartyResponse {
        return partyService.createParty(mapId, party)
    }

    @GetMapping
    fun getLeaderParty(): PartyResponse? {
        return partyService.getLeaderParty()
    }
}