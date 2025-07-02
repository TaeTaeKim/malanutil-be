package taeyun.malanalter.feignclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import taeyun.malanalter.config.exception.ErrorNotification


@FeignClient(name = "discordClient", url = "\${alerter.discord.webhook-url}")
interface DiscordAlertClient {

    @PostMapping
    fun sendAlarm(@RequestBody message: ErrorNotification)
}