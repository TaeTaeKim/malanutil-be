package taeyun.malanalter.auth.discord

import io.github.oshai.kotlinlogging.KotlinLogging
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.entities.UserSnowflake
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.alertitem.domain.AlertItemEntity
import taeyun.malanalter.alertitem.domain.AlertItemTable
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.domain.ItemBidTable
import taeyun.malanalter.config.exception.AlerterServerError
import taeyun.malanalter.user.domain.UserEntity
import java.util.*
import java.util.concurrent.ConcurrentHashMap

private val logger = KotlinLogging.logger { }

@Service
class DiscordService(
    val discordProperties: DiscordProperties
) {
    private val discord = JDABuilder.createDefault(discordProperties.botToken).build().awaitReady()
    private val userFailureCount = ConcurrentHashMap<Long, Int>()

    companion object {
        val URL_PATTERN = Regex("\\[링크]\\(https://mapleland\\.gg/trade/([^)]+)\\)")
        const val MAX_FAILURE_COUNT = 3
    }

    fun addUserToServer(discordUser: DiscordOAuth2User) {
        val randomUUID = UUID.randomUUID().toString()
        try {
            val userSnowflake = UserSnowflake.fromId(discordUser.getId().toString())
            val guild = discord.getGuildById(discordProperties.serverId)!!
            guild.addMember(discordUser.getToken(), userSnowflake).complete()
        } catch (e: Exception) {
            if (e is IllegalArgumentException || e.message.equals("User is already in this guild")) {
                logger.error { "$randomUUID Error in inviting user to server ${e.message} ${e.javaClass}" }
            } else {
                throw AlerterServerError(uuid = randomUUID, message = "Error in inviting user to server", rootCause = e)
            }

        }
    }

    /**
     * @return BID URL that successfully sent
     */
    fun sendDirectMessage(userId: Long, message: String) {

        discord.retrieveUserById(userId.toString()).queue(
            { user -> // onSuccess for user retrieval
                user.openPrivateChannel().queue(
                    { channel -> // onSuccess for channel opening
                        if (message.isNotBlank()) {

                            channel.sendMessage(message).queue(
                                { // onSuccess for message sending
                                    logger.debug { "Message sent successfully to user $userId" }
                                    userFailureCount.remove(userId) // Reset failure count on success
                                    val extractBidUrlFromMessage = extractBidUrlFromMessage(message)
                                    makeBidSent(userId, extractBidUrlFromMessage)
                                },
                                { error -> // onFailure for message sending
                                    logger.error {
                                        "Failed to send message to user $userId: ${error.message}\n message : ${message.substring(0,15)}..."
                                    }

                                    if (error.toString().contains("50007") || error.toString().contains("CANNOT_SEND_TO_USER")) {
                                        handleCannotSendToUserError(userId)
                                    }
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

    private fun extractBidUrlFromMessage(message: String): List<String> {
        return URL_PATTERN.findAll(message)
            .map { it.groupValues[1] }
            .toList()
    }

    fun makeBidSent(userId: Long, urls: List<String>) {
        if(urls.isEmpty()){
            return
        }
        transaction {
            val userAlertItemIdList = AlertItemEntity.find { AlertItemTable.userId eq userId }.map { it.getId() }
            ItemBidEntity.find {
                (ItemBidTable.alertItemId inList userAlertItemIdList) and (ItemBidTable.url inList urls)
            }.forEach { it.isSent = true }
        }

    }

    private fun handleCannotSendToUserError(userId: Long) {
        val currentCount = userFailureCount.getOrDefault(userId, 0) + 1
        userFailureCount[userId] = currentCount
        
        if (currentCount >= MAX_FAILURE_COUNT) {
            disableUser(userId)
            userFailureCount.remove(userId) // Clean up memory
        }
    }

    private fun disableUser(userId: Long) {
        transaction {
            UserEntity.findById(userId)?.let { user ->
                user.disabled = true
                logger.info { "User $userId has been disabled due to repeated CANNOT_SEND_TO_USER errors" }
            }
        }
    }

}