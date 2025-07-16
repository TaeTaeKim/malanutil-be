package taeyun.malanalter.timer.preset

import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.timer.preset.domain.PresetEntity
import taeyun.malanalter.timer.preset.domain.PresetItemTable
import taeyun.malanalter.timer.preset.domain.PresetTable
import taeyun.malanalter.timer.preset.dto.PresetDto
import taeyun.malanalter.timer.preset.dto.PresetSaveRequest
import taeyun.malanalter.user.UserService

@Service
class PresetService {

    fun getUserPreset(): List<PresetDto> {
        val loginUserId = UserService.getLoginUserId()
        return transaction {
            PresetEntity.find { PresetTable.userId eq loginUserId }.map {
                PresetDto.fromEntity(it)
            }
        }
    }

    fun savePreset(saveRequest: PresetSaveRequest) {
        val loginUserId = UserService.getLoginUserId()
        transaction {
            val newPreset = PresetEntity.new {
                name = saveRequest.name
                userId = loginUserId
            }

            PresetItemTable.batchInsert(saveRequest.items, shouldReturnGeneratedValues = false){
                this[PresetItemTable.presetId] = newPreset.id.value
                this[PresetItemTable.itemId] = it.itemId
                this[PresetItemTable.price] = it.price
            }
        }
    }
}