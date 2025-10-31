package taeyun.malanalter.party.character.dto

import org.jetbrains.exposed.v1.core.ResultRow
import taeyun.malanalter.party.character.CharacterTable

data class CharacterDto(
    val id: String,
    val name: String,
    val level: Int,
    val job: String,
    val isDefault: Boolean,
    val comment: String? = null,
){
    companion object{
        fun from(row: ResultRow): CharacterDto {
            return CharacterDto(
                id = row[CharacterTable.id].value,
                name = row[CharacterTable.name],
                level = row[CharacterTable.level],
                job = row[CharacterTable.job],
                isDefault = row[CharacterTable.isDefault],
                comment = row[CharacterTable.comment] ?: ""
            )
        }
    }
}