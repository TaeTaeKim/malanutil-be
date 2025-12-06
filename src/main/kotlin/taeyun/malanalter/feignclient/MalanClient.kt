package taeyun.malanalter.feignclient

import feign.Headers
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.SpringQueryMap
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.MalanggBidRequest

@FeignClient(name="malanClient", url="https://api.mapleland.gg/trade")
interface MalanClient {

    @GetMapping
    @Headers("User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
    fun getItemBidList(
        @RequestParam("itemCode") itemCode: Int,
        @SpringQueryMap malanggBidRequest: MalanggBidRequest
    ): List<ItemBidInfo>?
}