package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.party.pat.dto.*
import taeyun.malanalter.party.pat.service.PartyFinderService

/* 구직 유저 관련 컨트롤러 */
@RestController
@RequestMapping("/party/finder")
class PartyFinderController(
    val partyFinderService: PartyFinderService
){

    // 구직 유저가 선택한 맵의 파티를 반환하는 컨트롤러
    @GetMapping
    fun getParties(@RequestParam mapIds: List<Long>): List<PartyResponse>{
        return partyFinderService.getPartiesByMaps(mapIds)
    }

    @GetMapping("/discord")
    fun getMapDiscord(@RequestParam mapIds: List<Long>): Map<Long, List<DiscordMessageDto>> {
        return partyFinderService.getMapDiscordMessages(mapIds)
    }

    // 구직 유저가 요청한 맵 디스코드를 반환하는 컨트롤러
    @GetMapping("/mapDiscord")
    fun getPartyDiscord(mapIds: List<Long>){

    }

    /**********
     * 파티 지원 관련 API
     *********/
    // 구직 유저가 파티에 지원하는 기능
    @PostMapping("/apply")
    fun applyToParty(@RequestBody partyApplyRequest: PartyApplyRequest) {
        partyFinderService.applyParty(partyApplyRequest)
    }

    @GetMapping("/apply")
    fun getAppliedPosition(): List<AppliedPositionDto>{
        return partyFinderService.getAppliedPositions()
    }
    @DeleteMapping("/apply")
    fun cancelApplication(@RequestParam partyId: String, @RequestParam positionId: String) {
        partyFinderService.cancelApplication(partyId, positionId)
    }

    /**********
     * 인재풀 관련 API
     *********/
    @GetMapping("/talentPool")
    fun getRegisteringTalentPool(): RegisteringPoolResponse {
        return partyFinderService.getRegisteringPool()
    }

    @PostMapping("/talentPool")
    fun registerToTalentPool(@RequestBody request: TalentRegisterRequest) : Long {
        return partyFinderService.registerToTalentPool(request.mapId, request.characterId)
    }

    @DeleteMapping("/talentPool/{mapId}")
    fun deleteTalentPool(@PathVariable mapId: Long){
        partyFinderService.deleteTalentMap(mapId)
    }

    // 인재풀 활성 시간 갱신
    @PostMapping("/renew/heartbeat/{characterId}")
    fun renewHeartbeat(@PathVariable characterId: String) {
        partyFinderService.renewFinderHeartbeat(characterId)
    }

    /**********
     * 파티 초대 관련 API
     *********/
    // 구직 유저가 초대를 수락 거절 컨트롤러
    @PostMapping("/invite/respond")
    fun handleInvitation(@RequestParam invitationId: String){
        TODO()

    }
    // 받은 초대를 반환하는 컨트롤러
    @GetMapping("/invite")
    fun getInvitations(): List<Any>{
        TODO()
    }

    // 구직 유저가 파티에서 탈퇴하는 기능
    @PostMapping("/leave/{partyId}")
    fun leaveParty(@RequestParam partyId: String){
        TODO()
    }
}