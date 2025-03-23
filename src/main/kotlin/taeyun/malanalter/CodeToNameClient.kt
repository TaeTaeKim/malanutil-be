package taeyun.malanalter

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "codeToNameClient", url = "https://maplestory.io/api/gms/200/item/{itemCode}/name")
interface CodeToNameClient {

    @GetMapping
    fun getItemName(
        @PathVariable("itemCode") itemCode: String
    ): String
}