package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.domain.ItemBidEntity

data class ItemBidDto(
    val id: Long,
    val price: Long,
    val url: String,
    val comment: String?
) {
    companion object {
        fun from(entity: ItemBidEntity): ItemBidDto {
            return ItemBidDto(entity.id.value, entity.price, entity.url, entity.comment)
        }
    }
}
