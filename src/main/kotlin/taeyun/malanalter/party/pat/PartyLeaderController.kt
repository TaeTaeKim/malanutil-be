package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.party.pat.service.PartyLeaderService

@RestController
@RequestMapping("/party/leader")
class PartyLeaderController(
    val partyLeaderService: PartyLeaderService
) {

    /**************
     * 파티 관리 API
     ************/
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

    // 포지션 수정
    @PatchMapping("/{partyId}/position/{positionId}")
    fun updatePartyPosition(@RequestBody update: PositionUpdateReq, @PathVariable partyId: String, @PathVariable positionId: String) {
        partyLeaderService.updatePartyPosition(update, partyId, positionId)
    }
    /**************
     * 파티 활성화 관련 API
     ************/

    // @return : 음수(만료) 혹은 남은 시간 Sec
    @GetMapping("/heartbeat")
    fun getPartyHeartbeat(@RequestParam partyId: String) : Long{
        return partyLeaderService.getPartyHeartbeat(partyId)
    }

    @PostMapping("/renew/heartbeat/{partyId}")
    fun renewHeartbeat(@PathVariable partyId: String) {
        partyLeaderService.renewPartyHeartbeat(partyId)
    }



    /**************
     * 유저 초대 및 인재풀 조회 API
     ************/


    // 유저를 파티에 초대하는 API
    @PostMapping("/invite/{userId}")
    fun inviteUserToParty(@PathVariable userId: Long) {
        partyLeaderService.inviteUserToParty(userId)
    }

    @GetMapping("/talent")
    fun getTalentPool(@RequestParam mapId: Long, @RequestParam partyId: String): List<TalentResponse> {
        return partyLeaderService.getTalentPool(mapId, partyId)
    }

    @GetMapping("/applicant")
    fun getApplicant(): List<ApplicantRes> {
        return partyLeaderService.getApplicant()
    }

    @PostMapping("/accept/applicant")
    fun acceptApplicant(@RequestBody applyAcceptReq: ApplyAcceptReq): PositionDto {
        return partyLeaderService.acceptApplicant(applyAcceptReq)
    }


}