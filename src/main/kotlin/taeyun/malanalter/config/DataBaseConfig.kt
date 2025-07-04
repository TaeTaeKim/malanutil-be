package taeyun.malanalter.config

import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import taeyun.malanalter.config.property.DataSourceProperties

@Configuration
data class DataBaseConfig(val dbProperties: DataSourceProperties, val dataSource: HikariDataSource ) {
    @Bean
    fun database(): Database{
        return Database.connect(dataSource)
    }
}
