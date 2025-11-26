package taeyun.malanalter.party.chat

import kotlinx.datetime.toJavaInstant
import org.jetbrains.exposed.v1.core.ResultRow
import java.time.Instant

data class PartyChatDto(
    val id : String,
    val characterName: String,
    val positionName: String,
    val content:String,
    val createdAt: Instant,
){
    companion object{
        fun from(row: ResultRow): PartyChatDto {
            return PartyChatDto(
                id = row[PartyChatTable.id].value,
                characterName = row[PartyChatTable.characterName],
                positionName = row[PartyChatTable.positionName],
                content = row[PartyChatTable.content],
                createdAt = row[PartyChatTable.createdAt].toJavaInstant()
            )
        }
    }
}
