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
    val alertService: AlertService
) {

    @PostMapping
    fun save(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        alertRepository.save(itemId, itemCondition)
    }

    @PatchMapping
    fun update(@RequestParam alertId: Int, @RequestBody updateItemCondition: ItemCondition) {
        alertRepository.update(alertId, updateItemCondition)
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

    @GetMapping("/discord-test")
    fun discordTest() {
        alertService.sendTestDiscordMessage()
    }
}