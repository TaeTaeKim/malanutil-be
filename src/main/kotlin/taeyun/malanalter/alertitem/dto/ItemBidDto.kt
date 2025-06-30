package taeyun.malanalter.alertitem.dto

import taeyun.malanalter.alertitem.domain.ItemBidEntity

data class ItemBidDto(
    val bidId: String,
    val price: Long,
    val comment: String?
){
    companion object{
        fun from(entity: ItemBidEntity): ItemBidDto{
            return ItemBidDto(entity.id.value,entity.price, entity.comment)
        }
    }
}
