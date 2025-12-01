package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.character.CharacterEntity

/**
 * 유저가 지원시 파티장에게 응답되는 지원자 정보 DTO
 */
data class ApplicantRes(
    val actionType: ApplicantAction,
    val applyId: String? = null, // 초대를
    val applyUserId: String,
    val characterId: String? = null,
    val name: String? = null,
    val level: Int? = null,
    val job: String?= null,
    val comment: String?=null,
    val positionId: String,
){
    companion object{
        fun makeCancelRes(applicantUserId: String, positionId: String) : ApplicantRes{
            return ApplicantRes(
                actionType = ApplicantAction.CANCEL,
                applyUserId = applicantUserId,
                positionId = positionId,
            )
        }

        fun makeAcceptRes(acceptUserId: Long,positionId: String,  characterEntity: CharacterEntity): ApplicantRes{
            return ApplicantRes(
                actionType = ApplicantAction.ACCEPT,
                applyUserId = acceptUserId.toString(),
                characterId = characterEntity.id.value,
                name = characterEntity.name,
                level = characterEntity.level,
                job = characterEntity.job,
                positionId = positionId,
            )
        }
        fun makeLeaveRes(applicantUserId: String, positionId: String) : ApplicantRes{
            return ApplicantRes(
                actionType = ApplicantAction.LEAVE,
                applyUserId = applicantUserId,
                positionId = positionId,
            )
        }
    }
}
enum class ApplicantAction{
    ADD, CANCEL, NONE, ACCEPT, LEAVE
}
