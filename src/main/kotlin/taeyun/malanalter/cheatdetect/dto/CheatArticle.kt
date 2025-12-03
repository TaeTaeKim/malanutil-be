package taeyun.malanalter.cheatdetect.dto

import taeyun.malanalter.cheatdetect.CheatSearchDomain
import java.time.Instant

data class CheatArticle(
    val domain: CheatSearchDomain,
    val timestamp: Instant,
    val content: String,
    val link: String


)