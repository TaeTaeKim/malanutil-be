package taeyun.malanalter.party.character

import org.jetbrains.exposed.v1.core.Column
import org.jetbrains.exposed.v1.core.ReferenceOption
import org.jetbrains.exposed.v1.core.dao.id.EntityID
import org.jetbrains.exposed.v1.core.dao.id.IdTable
import org.jetbrains.exposed.v1.datetime.CurrentDateTime
import org.jetbrains.exposed.v1.datetime.datetime
import taeyun.malanalter.user.domain.Users


object CharacterTable : IdTable<String>(name = "character") {
    override val id: Column<EntityID<String>> = varchar("character_id", 255).entityId()
    val name = varchar("name", 50)
    val level = integer("level")
    val job = varchar("job", 50)
    val isDefault = bool("is_default").default(false)
    val comment = varchar("comment", 255).nullable()
    val userId = reference("user_id", Users.id, onDelete = ReferenceOption.CASCADE)
    val createdAt = datetime("created_at").defaultExpression(CurrentDateTime)
    val isActive = bool("is_active").default(false)

    override val primaryKey = PrimaryKey(id)

    init {
        index(isUnique = false, userId, id)
    }
}