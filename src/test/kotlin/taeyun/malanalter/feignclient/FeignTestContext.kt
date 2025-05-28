package taeyun.malanalter.feignclient

import org.springframework.boot.autoconfigure.ImportAutoConfiguration
import org.springframework.boot.autoconfigure.http.HttpMessageConvertersAutoConfiguration
import org.springframework.cloud.openfeign.FeignAutoConfiguration

@ImportAutoConfiguration(classes = [
    FeignAutoConfiguration::class,
    HttpMessageConvertersAutoConfiguration::class,
])
class FeignTestContext {

}