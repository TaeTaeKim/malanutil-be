package taeyun.malanalter.party.pat

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.PartyServerError
import taeyun.malanalter.party.pat.dto.RegisteringPoolResponse
import taeyun.malanalter.user.UserService
import java.util.UUID.randomUUID

val logger = KotlinLogging.logger {}
@Service
class PartyFinderService(val talentPoolService: TalentPoolService) {

    fun registerToTalentPool(mapId: Long, characterId: String): Long {
        try {

            talentPoolService.registerToTalentPool(mapId, characterId)
            // todo : pub sub 으로 publish 하는 기능 필요
            return mapId
        }catch (ex: Exception){
            val randomUUID = randomUUID()
            logger.error { "$randomUUID Error in inviting user to server ${ex.message} ${ex.javaClass}" }
            throw PartyServerError(uuid = randomUUID.toString(), message = "Error in inviting user to server", rootCause = ex)
        }

    }

    fun deleteTalentMap(mapId: Long) {
        talentPoolService.removeFromTalentPool(mapId)
    }

    fun renewFinderHeartbeat(characterId: String) {
        talentPoolService.renewHeartbeat(characterId)
    }

    fun getRegisteringPool(): RegisteringPoolResponse {
        val userId = UserService.getLoginUserId()
        return talentPoolService.getRegisteringMaps(userId)
    }

}