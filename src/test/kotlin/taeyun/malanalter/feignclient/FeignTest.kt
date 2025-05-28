package taeyun.malanalter.feignclient

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.cloud.openfeign.EnableFeignClients

@SpringBootTest(
    classes = [FeignTestContext::class, FeignTestConfig::class],
    properties = [
        "logging.level.taeyun.malanalter.feignclient=DEBUG",
    ]
)
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@EnableFeignClients
annotation class FeignTest()
