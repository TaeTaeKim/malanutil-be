package taeyun.malanalter


import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableFeignClients
class MalanalterApplication

fun main(args: Array<String>) {
	runApplication<MalanalterApplication>(*args)
}
