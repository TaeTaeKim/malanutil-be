package taeyun.malanalter.alertitem.service

import taeyun.malanalter.alertitem.domain.ItemBidEntity

object BidAlarmFilter {
    // 새로운 bid 이거나 한번도 전송되지 않은 알람일 경우 true
    fun isNotYetSent(existBidList: List<ItemBidEntity>, url: String) : Boolean {
        val existingBid = existBidList.find { it.url == url }
        return existingBid == null || !existingBid.isSent
    }

    // 새롭게 올라온 매물이거나 알람이 켜져있을 경우
    fun isAlarmEnabled(existBidList: List<ItemBidEntity>, url: String) : Boolean {
        val existingBid = existBidList.find { it.url == url }
        return existingBid == null || (existingBid.isAlarm)
    }

}