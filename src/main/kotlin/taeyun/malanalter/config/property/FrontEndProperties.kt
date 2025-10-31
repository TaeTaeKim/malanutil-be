package taeyun.malanalter.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "alerter.frontend")
data class FrontEndProperties @ConstructorBinding constructor(
    val redirectionUrl: String,
    val alerterCallbackUrl: String,
    val timerCallbackUrl: String,
    val patCallbackUrl: String,
)
