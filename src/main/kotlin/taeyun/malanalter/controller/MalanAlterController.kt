package taeyun.malanalter.controller

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.dto.ItemCondition
import taeyun.malanalter.dto.RegisteredItem
import taeyun.malanalter.repository.AlertRepository

@RestController
@RequestMapping("/malan-alter")
class MalanAlterController(
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
    fun delete(@RequestParam itemId: Int) {
        alertRepository.delete(itemId)
    }

    @GetMapping
    fun getCheckItemIdAndPriceMap(): List<RegisteredItem> {
        return alertRepository.getRegisteredItem()
    }
}