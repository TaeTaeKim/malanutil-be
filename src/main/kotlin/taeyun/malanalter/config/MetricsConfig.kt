package taeyun.malanalter.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Gauge
import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Timer
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicLong

@Component
class MetricsService(meterRegistry: MeterRegistry) {
    
    private val alertProcessingTimer = Timer.builder("alert.processing.duration")
        .description("Time taken to process alerts")
        .register(meterRegistry)
    
    private val discordApiCallCounter = Counter.builder("discord.api.calls.total")
        .description("Total number of Discord API calls")
        .register(meterRegistry)
    
    private val discordApiFailureCounter = Counter.builder("discord.api.failures.total")
        .description("Total number of Discord API failures")
        .register(meterRegistry)
    
    private val malanggApiCallCounter = Counter.builder("malangg.api.calls.total")
        .description("Total number of malangg.gg API calls")
        .register(meterRegistry)
    
    private val malanggApiFailureCounter = Counter.builder("malangg.api.failures.total")
        .description("Total number of malangg.gg API failures")
        .register(meterRegistry)
    
    private val malanggApiTimer = Timer.builder("malangg.api.duration")
        .description("Time taken for malangg.gg API calls")
        .register(meterRegistry)
    
    // Per-cycle metrics
    private val currentCycleApiCalls = AtomicLong(0)
    private val currentCycleApiFailures = AtomicLong(0)
    private val currentCycleDiscordCalls = AtomicLong(0)
    private val currentCycleDiscordFailures = AtomicLong(0)
    
    init {
        // Register gauges for current cycle metrics
        Gauge.builder("malangg.api.calls.current_cycle", currentCycleApiCalls) { it.get().toDouble() }
            .description("API calls in current processing cycle")
            .register(meterRegistry)
            
        Gauge.builder("malangg.api.failures.current_cycle", currentCycleApiFailures) { it.get().toDouble() }
            .description("API failures in current processing cycle")
            .register(meterRegistry)
            
        Gauge.builder("discord.api.calls.current_cycle", currentCycleDiscordCalls) { it.get().toDouble() }
            .description("Discord API calls in current processing cycle")
            .register(meterRegistry)
            
        Gauge.builder("discord.api.failures.current_cycle", currentCycleDiscordFailures) { it.get().toDouble() }
            .description("Discord API failures in current processing cycle")
            .register(meterRegistry)
    }
    
    fun resetCycleMetrics() {
        currentCycleApiCalls.set(0)
        currentCycleApiFailures.set(0)
        currentCycleDiscordCalls.set(0)
        currentCycleDiscordFailures.set(0)
    }
    
    fun recordAlertProcessingTime(durationMs: Long) {
        alertProcessingTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
    
    fun incrementDiscordApiCall() {
        discordApiCallCounter.increment()
        currentCycleDiscordCalls.incrementAndGet()
    }
    
    fun incrementDiscordApiFailure() {
        discordApiFailureCounter.increment()
        currentCycleDiscordFailures.incrementAndGet()
    }
    
    fun incrementMalanggApiCall() {
        malanggApiCallCounter.increment()
        currentCycleApiCalls.incrementAndGet()
    }
    
    fun incrementMalanggApiFailure() {
        malanggApiFailureCounter.increment()
        currentCycleApiFailures.incrementAndGet()
    }
    
    fun recordMalanggApiTime(durationMs: Long) {
        malanggApiTimer.record(durationMs, java.util.concurrent.TimeUnit.MILLISECONDS)
    }
}