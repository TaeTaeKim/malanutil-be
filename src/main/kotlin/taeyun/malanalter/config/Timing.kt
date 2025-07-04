package taeyun.malanalter.config

import io.github.oshai.kotlinlogging.KotlinLogging
import java.util.concurrent.TimeUnit


inline fun <T> measureExecutionTime(
    tag: String = "",
    block: () -> T
): T {
    val timeLogger = KotlinLogging.logger {  }
    val start = System.nanoTime()
    val result = block()
    val elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start)
    timeLogger.info { if (tag.isNotBlank()) "$tag executed in ${elapsedMs}ms" else "Executed block in ${elapsedMs}ms" }
    return result
}