package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.pat.dao.PositionStatus

// todo : 생성자에서 hasPositions가 true일 때 positions가 비어있으면 안됨 검증 추가
// todo : numPeople가 positions.size과 일치하는지 검증 추가
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
    val description: String,
    val price: String,
    val isLeader: Boolean,
    val preferJob: List<String>,
    val status: PositionStatus,
    val isPriestSlot: Boolean
)

