package taeyun.malanalter.alertitem

import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.*
import taeyun.malanalter.alertitem.dto.ItemBidDto
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.user.UserService

@RestController
@RequestMapping("/alerter")
class MalanAlerterController(
    val alertRepository: AlertRepository,
    val alertService: AlertService
) {

    @PostMapping
    fun save(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        alertService.saveNewAlertItem(itemId, itemCondition)
    }

    @PatchMapping
    @Operation(description = "사용자가 등록한 알람 아이템의 옵션을 변경하는 API")
    fun update(@RequestParam alertId: Int, @RequestBody updateItemCondition: ItemCondition) {
        alertRepository.update(alertId, updateItemCondition)
    }

    @DeleteMapping
    @Operation(description = "알람 아이템을 삭제하는 API")
    fun delete(@RequestParam alertId: Int) {
        alertRepository.delete(alertId)
    }

    @GetMapping
    fun getRegisteredItem(): List<RegisteredItem> {
        return alertRepository.getRegisteredItem()
            .filter { it.userId == UserService.getLoginUserId() }
    }

    @GetMapping("/bid")
    @Operation(description = "사용자가 등록한 아이템들의 bid를 반환하는 API")
    fun getBids(): Map<Int, List<ItemBidDto>> {
        return alertService.getAllBidOfUser()
    }

    @DeleteMapping("/bid/{bidId}")
    @Operation(description = "bid의 알림을 끄는 API")
    fun turnOffBids(@PathVariable bidId: Long) {
        return alertService.turnOffBid(bidId)
    }

    @PatchMapping("/toggle/{alertId}")
    fun toggleAlarm(@PathVariable alertId: Int) {
        alertRepository.toggleItemAlarm(alertId)
    }

    @GetMapping("/discord-test")
    fun discordTest() {
        alertService.sendTestDiscordMessage()
    }
}