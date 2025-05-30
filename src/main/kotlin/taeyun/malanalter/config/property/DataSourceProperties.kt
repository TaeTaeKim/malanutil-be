package taeyun.malanalter.config.property

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.bind.ConstructorBinding

@ConfigurationProperties(prefix = "spring.datasource")
data class DataSourceProperties @ConstructorBinding constructor(
    val url: String,
    val username: String,
    val password: String,
    val driverClassName: String = "org.postgresql.Driver"
)
