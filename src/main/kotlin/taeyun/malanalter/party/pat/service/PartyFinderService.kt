package taeyun.malanalter.party.pat.service

import io.github.oshai.kotlinlogging.KotlinLogging
import org.jetbrains.exposed.v1.core.StdOutSqlLogger
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.jdbc.addLogger
import org.jetbrains.exposed.v1.jdbc.selectAll
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.PartyServerError
import taeyun.malanalter.party.pat.dao.PartyStatus
import taeyun.malanalter.party.pat.dao.PartyTable
import taeyun.malanalter.party.pat.dao.PositionTable
import taeyun.malanalter.party.pat.dto.PartyResponse
import taeyun.malanalter.party.pat.dto.PositionDto
import taeyun.malanalter.party.pat.dto.RegisteringPoolResponse
import taeyun.malanalter.user.UserService
import java.util.UUID.randomUUID

val logger = KotlinLogging.logger {}

@Service
class PartyFinderService(val talentPoolService: TalentPoolService, private val partyRedisService: PartyRedisService) {

    fun registerToTalentPool(mapId: Long, characterId: String): Long {
        try {

            talentPoolService.registerToTalentPool(mapId, characterId)
            // todo : pub sub 으로 publish 하는 기능 필요
            return mapId
        } catch (ex: Exception) {
            val randomUUID = randomUUID()
            logger.error { "$randomUUID Error in inviting user to server ${ex.message} ${ex.javaClass}" }
            throw PartyServerError(
                uuid = randomUUID.toString(),
                message = "Error in inviting user to server",
                rootCause = ex
            )
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

    fun getPartiesByMaps(mapIds: List<Long>): List<PartyResponse> {
        return transaction {
            addLogger(StdOutSqlLogger)
            PartyTable.selectAll()
                .where { PartyTable.mapId inList mapIds and (PartyTable.status eq PartyStatus.RECRUITING) }
                .filter { partyRedisService.getPartyTTL(it[PartyTable.id].value) > 0 }
                .map{
                    val positions = PositionTable.selectAll()
                        .where { PositionTable.partyId eq it[PartyTable.id].value }
                        .map(PositionDto::from)
                    PartyResponse.withPositions(it, positions)
                }
        }

    }

}