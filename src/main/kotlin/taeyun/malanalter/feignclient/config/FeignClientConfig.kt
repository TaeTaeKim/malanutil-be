package taeyun.malanalter.feignclient.config

import feign.Feign
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FeignClientConfig {

    @Bean
    fun feignBuilder(): Feign.Builder{
        return Feign.builder()
    }
}