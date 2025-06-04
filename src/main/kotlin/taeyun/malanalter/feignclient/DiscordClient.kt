package taeyun.malanalter.feignclient

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import taeyun.malanalter.alertitem.dto.DiscordMessage


interface DiscordClient {

    @PostMapping
    fun sendDiscordMessage(
        @RequestBody message: DiscordMessage
    )
}