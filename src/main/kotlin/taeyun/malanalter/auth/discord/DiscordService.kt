package taeyun.malanalter.auth.discord

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.UserSnowflake
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.AlerterServerError
import java.util.*

private val logger = KotlinLogging.logger {  }

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
            if(e is IllegalArgumentException || e.message.equals("User is already in this guild")){
                logger.error { "$randomUUID Error in inviting user to server ${e.message} ${e.javaClass}" }
            }else{
                throw AlerterServerError(uuid = randomUUID, message = "Error in inviting user to server", rootCause = e)
            }

        }
    }

    fun sendDirectMessage(userId: Long, message: String) {
        try {
            discord.retrieveUserById(userId.toString()).queue { user ->
                user.openPrivateChannel().queue { channel ->
                    if (message.isNotBlank()) {
                        channel.sendMessage(message).queue()
                    }
                }
            }
        } catch (e: Exception) {
            logger.error { "유저 $userId 에게 메세지 전송실패 메세지 $message" }
            throw AlerterServerError(uuid = "NO UUID", message = "Error in Sending Message to User", rootCause = e)
        }
    }


}