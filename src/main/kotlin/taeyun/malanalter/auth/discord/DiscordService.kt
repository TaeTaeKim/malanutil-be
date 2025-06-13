package taeyun.malanalter.auth.discord

import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.UserSnowflake
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.AlerterServerError
import java.util.*

@Service
class DiscordService(
    val discordProperties: DiscordProperties
) {
    private val discord = JDABuilder.createDefault(discordProperties.botToken).build().awaitReady()

    fun addUserToServer(discordUser: DiscordOAuth2User) {
        val randomUUID = UUID.randomUUID().toString()
        try {
            val userSnowflake = UserSnowflake.fromId(discordUser.getId().toString())
            val guild = discord.getGuildById(discordProperties.serverId)!!
            guild.addMember(discordUser.getToken(), userSnowflake).queue()
        } catch (e: Exception) {
            println(e.message)
            throw AlerterServerError(uuid = randomUUID, message = "Error in inviting user to server", rootCause = e)
        }
    }

    fun sendDirectMessage(userId: Long, message: String) {
        discord.retrieveUserById(userId.toString()).queue { user ->
            user.openPrivateChannel().queue { channel ->
                if(message.isNotBlank()){
                    channel.sendMessage(message).queue()
                }
            }
        }
    }


}