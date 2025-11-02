package taeyun.malanalter.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.redis")
data class RedisProperties @ConstructorBinding constructor(
    val host: String,
    val port: Int
)
