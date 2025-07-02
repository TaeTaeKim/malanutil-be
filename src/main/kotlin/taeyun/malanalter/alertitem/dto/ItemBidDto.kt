package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.domain.ItemBidEntity

data class ItemBidDto(
    val url: String,
    val price: Long,
    val comment: String?
){
    companion object{
        fun from(entity: ItemBidEntity): ItemBidDto{
            return ItemBidDto(entity.url,entity.price, entity.comment)
        }
    }
}
