package taeyun.malanalter.party.pat.dto


import java.time.LocalDateTime
import java.time.ZoneOffset

/**
 * 파티장을 위한 인재풀 DTO
 * @see TalentDto 에서 lastSent가 추가 되었음.
 * @property userId : 직렬화에서 userId가 overflow 되어서 값이 잘 안가기에 String 으로 설정
 */
data class TalentResponse(
    val userId: String,
    val characterId: String,
    val lastSent: Long?,
    val name: String,
    val level: Int,
    val job: String,
    val comment: String?
){
    companion object{
        fun from(dto: TalentDto, inviteTime: LocalDateTime?): TalentResponse {
            return TalentResponse(
                userId = dto.userId.toString(),
                characterId = dto.characterId,
                name = dto.name,
                lastSent = inviteTime?.atZone(ZoneOffset.UTC)?.toEpochSecond(),
                level = dto.level,
                job = dto.job,
                comment = dto.comment
            )
        }
    }
}