package taeyun.malanalter.timer.preset

import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.timer.preset.domain.PresetEntity
import taeyun.malanalter.timer.preset.domain.PresetTable
import taeyun.malanalter.timer.preset.dto.PresetDto
import taeyun.malanalter.user.UserService

@Service
class PresetService {

    fun getUserPreset(): List<PresetDto> {
        val loginUserId = UserService.getLoginUserId()
        return transaction {
            PresetEntity.find { PresetTable.userId eq loginUserId }.map{
                PresetDto.fromEntity(it)
            }
        }
    }
}