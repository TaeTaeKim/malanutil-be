package taeyun.malanalter.party.pat.dto

import java.time.ZonedDateTime

data class DiscordMessageDto(
    val content: String,
    val author: DiscordAuthor,
    val timestamp: ZonedDateTime
)
