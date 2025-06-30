package taeyun.malanalter.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "alerter.discord")
data class DiscordProperties @ConstructorBinding constructor(
    val webhookUrl: String
)
