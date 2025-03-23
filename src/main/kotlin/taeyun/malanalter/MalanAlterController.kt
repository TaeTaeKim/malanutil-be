package taeyun.malanalter

import org.springframework.web.bind.annotation.*
import java.util.concurrent.ConcurrentHashMap

@RestController
@RequestMapping("/malan-alter")
class MalanAlterController(
    val checkRepository: CheckRepository,
    val codeToNameClient: CodeToNameClient,
) {

    @PostMapping
    fun save(@RequestParam itemId: String, @RequestBody itemCondition: ItemCondition) {
        checkRepository.save(itemId, itemCondition)
    }

    @PatchMapping
    fun update(@RequestParam itemId: String, @RequestBody itemCondition: ItemCondition) {
        checkRepository.update(itemId, itemCondition)
    }

    @DeleteMapping
    fun delete(@RequestParam itemId: String) {
        checkRepository.delete(itemId)
    }

    @GetMapping
    fun getCheckItemIdAndPriceMap(): Map<String, ItemCondition> {
        val checkItemIdAndPriceMap = checkRepository.getCheckItemIdAndPriceMap()
        return checkItemIdAndPriceMap.map { (itemId, itemCondition) ->
            val itemName = checkRepository.getItemName(itemId)
            itemName to itemCondition
        }.toMap()
    }
}