package taeyun.malanalter.feignclient

import feign.Logger
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean

@TestConfiguration
class FeignTestConfig {
    @Bean
    fun feignLoggerLevel() : Logger.Level {
        return Logger.Level.FULL // Set the desired logging level for Feign clients
    }
}