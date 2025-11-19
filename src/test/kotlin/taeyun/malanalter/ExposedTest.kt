package taeyun.malanalter

import org.jetbrains.exposed.v1.spring.boot.autoconfigure.ExposedAutoConfiguration
import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.autoconfigure.jdbc.DataSourceTransactionManagerAutoConfiguration
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Import
import org.springframework.test.context.TestPropertySource
import taeyun.malanalter.config.TestDatabaseConfig


@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@TestConfiguration
@Import(TestDatabaseConfig::class)
@ImportAutoConfiguration(
    DataSourceAutoConfiguration::class,
    DataSourceTransactionManagerAutoConfiguration::class,
    ExposedAutoConfiguration::class
)
@TestPropertySource(
    properties = [
        "spring.datasource.url=jdbc:postgresql://localhost:5433/postgres?rewriteBatchedInserts=true",
        "spring.datasource.username=postgres",
        "spring.datasource.password=test"
    ]
)
annotation class ExposedTest()
