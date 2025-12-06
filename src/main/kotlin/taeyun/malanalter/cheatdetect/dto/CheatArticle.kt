package taeyun.malanalter.cheatdetect.dto

import taeyun.malanalter.cheatdetect.CheatSearchDomain
import taeyun.malanalter.cheatdetect.cheatagent.MalantalkResponse

data class CheatArticle(
    val domain: CheatSearchDomain,
    val count: Int,
    val link: String?,
){
    companion object{
        fun fromMalanTalkRes(res: MalantalkResponse): CheatArticle {
            return CheatArticle(
                CheatSearchDomain.MALANTALK,
                count = res.totalCount,
                link = "https://xn--lj2bo5cl56a.com/search"
            )
        }
        fun makeEmpty(domain: CheatSearchDomain): CheatArticle {
            return CheatArticle(
                domain = domain,
                count = 0,
                link = null
            )
        }
    }
}