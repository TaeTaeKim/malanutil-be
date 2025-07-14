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
            guild.addMember(discordUser.getToken(), userSnowflake).complete()
        } catch (e: Exception) {
            if(e is IllegalArgumentException || e.message.equals("User is already in this guild")){
                logger.error { "$randomUUID Error in inviting user to server ${e.message} ${e.javaClass}" }
            }else{
                throw AlerterServerError(uuid = randomUUID, message = "Error in inviting user to server", rootCause = e)
            }

        }
    }

    fun sendDirectMessage(userId: Long, message: String) {
        discord.retrieveUserById(userId.toString()).queue(
            { user -> // onSuccess for user retrieval
                user.openPrivateChannel().queue(
                    { channel -> // onSuccess for channel opening
                        if (message.isNotBlank()) {
                            channel.sendMessage(message).queue(
                                { // onSuccess for message sending
                                    logger.debug { "Message sent successfully to user $userId" }
                                },
                                { error -> // onFailure for message sending
                                    logger.error { "Failed to send message to user $userId: ${error.message}\n message : ${message.substring(0,15)}" }
                                }
                            )
                        }
                    },
                    { error -> // onFailure for channel opening
                        logger.error { "Failed to open private channel for user $userId: ${error.message}" }
                    }
                )
            },
            { error -> // onFailure for user retrieval
                logger.error { "Failed to retrieve user $userId: ${error.message}" }
            }
        )
    }


}