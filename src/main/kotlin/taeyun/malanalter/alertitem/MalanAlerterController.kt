package taeyun.malanalter.alertitem

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.alertitem.dto.ItemCondition
import taeyun.malanalter.alertitem.dto.RegisteredItem
import taeyun.malanalter.alertitem.repository.AlertRepository
import taeyun.malanalter.user.UserService

@RestController
@RequestMapping("/alerter")
class MalanAlerterController(
    val alertRepository: AlertRepository,
) {

    @PostMapping
    fun save(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        alertRepository.save(itemId, itemCondition)
    }

    @PatchMapping
    fun update(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        alertRepository.update(itemId, itemCondition)
    }

    @DeleteMapping
    fun delete(@RequestParam alertId: Int) {
        alertRepository.delete(alertId)
    }

    @GetMapping
    fun getCheckItemIdAndPriceMap(): List<RegisteredItem> {
        return alertRepository.getRegisteredItem()
            .filter { it.userId == UserService.getLoginUserId() }
    }

    @PatchMapping("/toggle/{alertId}")
    fun toggleAlarm(@PathVariable alertId: Int) {
        alertRepository.toggleItemAlarm(alertId)
    }
}