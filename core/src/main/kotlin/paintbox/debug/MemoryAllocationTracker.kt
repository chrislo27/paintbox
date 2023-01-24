package paintbox.debug

import paintbox.util.MemoryUtils
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import kotlin.math.round
import kotlin.math.roundToLong


class MemoryAllocationTracker {

    companion object {

        private val UPDATE_INTERVAL_MS: Long = 1000L
        private val GC_BEANS: List<GarbageCollectorMXBean> = ManagementFactory.getGarbageCollectorMXBeans()
    }

    private var lastAllocRate: Long = 0L
    private var lastMsTime: Long = 0L
    private var lastGcCount: Long = 0L
    private var lastHeapUsage: Long = 0L

    fun getBytesAllocatedPerSec(): Long {
        val msTime = System.currentTimeMillis()
        if (msTime - this.lastMsTime < UPDATE_INTERVAL_MS) {
            return this.lastAllocRate
        }

        val heapUsage = MemoryUtils.usedMemoryB
        val gcCount = GC_BEANS.sumOf { it.collectionCount }
        if (this.lastMsTime != 0L && gcCount == this.lastGcCount) { // If a gc happened, ignore it this time
            val elapsedTimeMs = 1000.0 / (msTime - this.lastMsTime)
            val heapDiff = heapUsage - this.lastHeapUsage
            this.lastAllocRate = (heapDiff * elapsedTimeMs).roundToLong()
        }

        this.lastMsTime = msTime
        this.lastHeapUsage = heapUsage
        this.lastGcCount = gcCount

        return this.lastAllocRate
    }

}
