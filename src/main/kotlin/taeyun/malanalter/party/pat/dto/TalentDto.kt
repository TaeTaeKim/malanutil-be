package taeyun.malanalter.party.pat.dto

import taeyun.malanalter.party.character.CharacterEntity

data class TalentDto(
    val userId: Long,
    val characterId: String,
    val name: String,
    val level: Int,
    val job: String,
    val comment: String?,
){
    companion object {
        fun fromEntity(e : CharacterEntity): TalentDto {
            return TalentDto(
                userId = e.userId.value,
                characterId = e.id.value,
                name = e.name,
                level = e.level,
                job = e.job,
                comment = e.comment,
            )
        }
    }
}
