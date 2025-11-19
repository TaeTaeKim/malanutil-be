package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.character.CharacterEntity

/**
 * 유저가 지원시 파티장에게 응답되는 지원자 정보 DTO
 */
data class ApplicantRes(
    val actionType: ApplicantAction,
    val applyId: String?,
    val applyUserId: String,
    val characterId: String,
    val name: String,
    val level: Int,
    val job: String,
    val comment: String?,
    val positionId: String,
){
    companion object{
        fun makeCancelRes(applicantUserId: String, positionId: String) : ApplicantRes{
            return ApplicantRes(
                actionType = ApplicantAction.CANCEL,
                applyId = null,
                applyUserId = applicantUserId,
                characterId = "",
                name = "",
                level = 0,
                job = "",
                comment = null,
                positionId = positionId,
            )
        }

        fun makeAcceptRes(acceptUserId: Long,positionId: String,  characterEntity: CharacterEntity): ApplicantRes{
            return ApplicantRes(
                actionType = ApplicantAction.ACCEPT,
                applyId = null,
                applyUserId = acceptUserId.toString(),
                characterId = characterEntity.id.value,
                name = characterEntity.name,
                level = characterEntity.level,
                job = characterEntity.job,
                comment = null,
                positionId = positionId,
            )
        }
    }
}
enum class ApplicantAction{
    ADD, CANCEL, NONE, ACCEPT
}
