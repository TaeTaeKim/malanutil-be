package taeyun.malanalter.auth.discord

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "discord")
data class DiscordProperties @ConstructorBinding constructor(
    val botToken: String,
    val serverId: String
)
