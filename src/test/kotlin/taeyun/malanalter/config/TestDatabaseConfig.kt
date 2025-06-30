package taeyun.malanalter.config

import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import taeyun.malanalter.alertitem.domain.ItemBidTable
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.user.domain.Users
import javax.sql.DataSource

@TestConfiguration
class TestDatabaseConfig {
    @Bean
    fun testDatabase(
        dataSource: DataSource
    ): Database {
        val db = Database.connect(dataSource)
        transaction {
            SchemaUtils.create(
                AlertItemTable,
                ItemBidTable,
                Users,
            )
        }
        return db
    }


}