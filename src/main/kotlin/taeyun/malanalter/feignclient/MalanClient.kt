package taeyun.malanalter.feignclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import taeyun.malanalter.dto.ItemBidInfo
import taeyun.malanalter.dto.MalanggBidRequest

@FeignClient(name="malanClient", url="https://api.mapleland.gg/trade")
interface MalanClient {

    @GetMapping
    fun getItemBidList(
        @RequestParam("itemCode") itemCode: Int,
        @SpringQueryMap malanggBidRequest: MalanggBidRequest
    ): List<ItemBidInfo>
}