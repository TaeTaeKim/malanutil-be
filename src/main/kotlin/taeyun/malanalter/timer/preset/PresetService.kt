package taeyun.malanalter.timer.preset

import org.jetbrains.exposed.v1.core.SortOrder
import org.jetbrains.exposed.v1.core.and
import org.jetbrains.exposed.v1.dao.with
import org.jetbrains.exposed.v1.jdbc.batchInsert
import org.jetbrains.exposed.v1.jdbc.transactions.transaction
import org.springframework.stereotype.Service
import taeyun.malanalter.config.exception.ErrorCode
import taeyun.malanalter.config.exception.TimerBadRequest
import taeyun.malanalter.timer.preset.domain.PresetEntity
import taeyun.malanalter.timer.preset.domain.PresetItemEntity
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
            PresetEntity.find { PresetTable.userId eq loginUserId }
                .orderBy(PresetTable.createdAt to SortOrder.DESC)
                .map { PresetDto.fromEntity(it) }
        }
    }

    fun savePreset(saveRequest: PresetSaveRequest) {
        val loginUserId = UserService.getLoginUserId()
        transaction {
            // check count of preset of user
            val presetCount = PresetEntity.find { PresetTable.userId eq loginUserId }.count()
            if (presetCount >= 5) {
                // remove oldest preset
                val oldestPreset = PresetEntity.find { PresetTable.userId eq loginUserId }
                    .orderBy(PresetTable.createdAt to SortOrder.ASC)
                    .firstOrNull()
                oldestPreset?.delete()
            }

            val newPreset = PresetEntity.new {
                name = saveRequest.name
                userId = loginUserId
            }

            PresetItemTable.batchInsert(saveRequest.items, shouldReturnGeneratedValues = false) {
                this[PresetItemTable.presetId] = newPreset.id.value
                this[PresetItemTable.itemId] = it.itemId
                this[PresetItemTable.price] = it.price
                if(it.isCustom && it.customItemName.isNullOrBlank()){
                    throw TimerBadRequest(ErrorCode.BAD_REQUEST, "커스텀아이템은 이름이 필수입니다.")
                }
                this[PresetItemTable.isCustom] = it.isCustom
                this[PresetItemTable.customItemName] = it.customItemName

            }
        }
    }

    fun deletePreset(presetId: Long) {
        val loginUserId = UserService.getLoginUserId()
        transaction {
            PresetEntity.find { PresetTable.userId eq loginUserId and (PresetTable.id eq presetId) }.firstOrNull()
                ?.delete()
        }
    }

    fun getPresetItems(presetId: Long): List<PresetItemEntity> {
        val loginUserId = UserService.getLoginUserId()
        return transaction {
            PresetEntity.find { PresetTable.userId eq loginUserId and (PresetTable.id eq presetId) }
                .with(PresetEntity::items)
                .firstOrNull()?.items?.toList() ?: emptyList()
        }
    }
}