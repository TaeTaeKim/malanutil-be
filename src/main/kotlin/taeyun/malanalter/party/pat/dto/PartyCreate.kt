package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.pat.dao.PositionStatus

data class PartyCreate(
    val characterId: String, // 파티장 캐릭터 ID
    val hasPositions: Boolean, // 커파, 개미굴 처럼 구인만 하는 파티여부
    val description: String, // 파티 설명
    val positions: List<Position>,
    val channel: String,
    val numPeople: Int, // 모집 인원수
    val discordNotification: Boolean, // 디스코드 알림 여부
    val includesPriest: Boolean // 프리 포함 여부
)

data class Position(
    val name: String, // 포지션 이름 (1층, 좌우깐)
    val description: String, // 심비, 지참금 등 설명
    val isLeader: Boolean,
    val preferJob: List<String>,
    val status: PositionStatus,
    val isPriestSlot: Boolean
)

