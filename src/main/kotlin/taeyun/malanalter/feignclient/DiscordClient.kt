package taeyun.malanalter.feignclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import taeyun.malanalter.alertitem.dto.DiscordMessage

@FeignClient(name = "discordClient", url = "https://discord.com/api/webhooks/1353080062894805115/lJEaD3EsXdC7jDKypb5JGUXNnkcw-_W8OpMUNL9qnyFj3xa8e_AlsMZ_BmO7dcs8guJK")
interface DiscordClient {

    @PostMapping
    fun sendDiscordMessage(
        @RequestBody message: DiscordMessage
    )
}