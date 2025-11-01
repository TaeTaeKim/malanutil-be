package taeyun.malanalter.party.pat

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import taeyun.malanalter.party.pat.dto.PartyResponse

/* 구직 유저 관련 컨트롤러 */
@RestController
@RequestMapping("/party/finder")
class PartyFinderController {

    // 구직 유저가 요청하는 파티를 반환하는 컨트롤러
    @GetMapping
    fun getParties(mapIds: List<Long>): List<PartyResponse>{
        return emptyList()
    }

    // 구직 유저가 요청한 맵 디스코드를 반환하는 컨트롤러
    @GetMapping("/mapDiscord")
    fun getPartyDiscord(mapIds: List<Long>){

    }

    // 구직 유저가 파티에 지원하는 기능

    // 특정 맵의 인재풀 등록 기능

    // 구직 유저가 초대를 수락 거절 컨트롤러

    // 구직 유저가 파티에서 탈퇴하는 기능




}