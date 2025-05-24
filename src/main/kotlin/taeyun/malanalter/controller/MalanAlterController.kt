package taeyun.malanalter.controller

import org.springframework.web.bind.annotation.*
import taeyun.malanalter.dto.ItemCondition
import taeyun.malanalter.dto.RegisteredItem
import taeyun.malanalter.repository.CheckRepository

@RestController
@RequestMapping("/malan-alter")
class MalanAlterController(
    val checkRepository: CheckRepository,
) {

    @PostMapping
    fun save(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        checkRepository.save(itemId, itemCondition)
    }

    @PatchMapping
    fun update(@RequestParam itemId: Int, @RequestBody itemCondition: ItemCondition) {
        checkRepository.update(itemId, itemCondition)
    }

    @DeleteMapping
    fun delete(@RequestParam itemId: Int) {
        checkRepository.delete(itemId)
    }

    @GetMapping
    fun getCheckItemIdAndPriceMap(): List<RegisteredItem> {
        return checkRepository.getRegisteredItem()
    }
}