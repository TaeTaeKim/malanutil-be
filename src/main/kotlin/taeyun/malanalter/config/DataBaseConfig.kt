package taeyun.malanalter.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.jetbrains.exposed.v1.jdbc.Database
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import taeyun.malanalter.config.property.DataSourceProperties
import javax.sql.DataSource

@Configuration
data class DataBaseConfig(val dbProperties: DataSourceProperties ) {

    @Bean
    fun datasource(): DataSource{
        val config =  HikariConfig().apply {
            jdbcUrl = dbProperties.url
            username = dbProperties.username
            password = dbProperties.password
            maximumPoolSize = 10
            driverClassName = dbProperties.driverClassName
            minimumIdle = 5
            idleTimeout = 60000 // 60 seconds
            connectionTimeout = 30000 // 30 seconds
            validationTimeout = 5000 // 5 seconds
        }
        return  HikariDataSource(config)
    }
    @Bean
    fun database(): Database{
        return Database.connect(datasource())
    }
}
