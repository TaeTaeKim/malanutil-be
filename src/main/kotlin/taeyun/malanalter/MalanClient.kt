package taeyun.malanalter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(name="malanClient", url="https://api.mapleland.gg/trade")
interface MalanClient {

    @GetMapping
    fun getItemBidList(
        @RequestParam("itemCode") itemCode: String,
        @SpringQueryMap itemCondition: ItemCondition
    ): List<ItemBidInfo>
}