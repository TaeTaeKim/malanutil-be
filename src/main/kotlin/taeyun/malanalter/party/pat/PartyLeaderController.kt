package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.pat.dto.PartyCreate
import taeyun.malanalter.party.pat.dto.PartyResponse

@RestController
@RequestMapping("/party/leader")
class PartyLeaderController(
    val partyLeaderService: PartyLeaderService
) {

    // 파티 생성
    @PostMapping("/{mapId}")
    fun createParty(@PathVariable mapId: Long, @RequestBody party: PartyCreate) : PartyResponse {
        return partyLeaderService.createParty(mapId, party)
    }

    // 로그인 유저가 리더인 파티 조회
    @GetMapping
    fun getLeaderParty(): PartyResponse? {
        return partyLeaderService.getLeaderParty()
    }

    // 로그인 유저의 특정 맵 파티 생성 이력 조회 -> 가장 최근 생성된 파티 이력 반환
    // 파티 생성 시 빠른 입력
    @GetMapping("/history/{mapId}")
    fun getPartyCreationHistory(@PathVariable mapId: Long): PartyCreate? {
        return partyLeaderService.getPartyCreationHistory(mapId)
    }

    // 파티 없애기
    @DeleteMapping("/{partyId}")
    fun deleteParty(@PathVariable partyId: String) {
        partyLeaderService.deleteParty(partyId)
    }

    @PostMapping("/invite/{userId}")
    fun inviteUserToParty(@PathVariable userId: Long) {
        partyLeaderService.inviteUserToParty(userId)
    }

    // todo : 이미 받은 포지션의 중복 신청 방지 필요
    @PostMapping("/apply/{applyId}")
    fun handlePartyApplication(@PathVariable applyId: Long) {

    }

    // 특정 맵의 인재풀을 조회하는 API
    @GetMapping("/talent/{mapId}")
    fun getTalentPool(@PathVariable mapId: Long)  {

    }

}