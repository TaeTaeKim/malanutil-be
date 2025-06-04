package taeyun.malanalter.feignclient

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import taeyun.malanalter.alertitem.dto.MapleItem

@FeignClient(name = "itemListClient", url = "https://mapleland.gg/api/items")
interface ItemListClient {

    @GetMapping
    fun getAllItemList(): List<MapleItem>
}