package taeyun.malanalter

import kotlinx.coroutines.Job
import taeyun.malanalter.alertitem.domain.ItemBidEntity
import taeyun.malanalter.alertitem.dto.ItemBidInfo
import taeyun.malanalter.alertitem.dto.RegisteredItem

/**
 * 아이템 체크 및 알람 발송의 핵심 로직을 추상화한 인터페이스.
 * 스케줄링, 코루틴 구현 등 인프라 관심사와 비즈니스 로직을 분리한다.
 */
interface ItemChecker {

    /** 등록된 아이템을 확인하고 알람을 발송하는 Job을 실행한다. */
    fun checkItem(): Job

    /** 특정 아이템의 비드를 조회하고 알람 대상 비드 목록을 반환한다. */
    suspend fun requestItemBids(item: RegisteredItem, existBidList: List<ItemBidEntity>): List<ItemBidInfo>
}