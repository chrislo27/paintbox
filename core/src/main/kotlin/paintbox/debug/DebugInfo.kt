package paintbox.debug

import paintbox.util.DecimalFormats
import java.text.NumberFormat

class DebugInfo {

    private var lastFrameTime: Long = -1L // Alternative to gdx framerate, which doesn't update when window is resized
    private var frameCounterStart: Long = 0L
    private var frameAcc: Int = 0
    private var mspfAvgAcc: Float = 0f
    private var mspfMinAcc: Float = -1f
    private var mspfMaxAcc: Float = -1f

    var fps: Int = 1
        private set
    var mspfAvg: Float = 1f
        private set
    var mspfMin: Float = 1f
        private set
    var mspfMax: Float = 1f
        private set
    val msNumberFormat: NumberFormat = DecimalFormats["0.00"]

    val memoryNumberFormat: NumberFormat = NumberFormat.getIntegerInstance()
    private val allocationTracker: MemoryAllocationTracker = MemoryAllocationTracker()
    var lastAllocationRateB: Long = 0L
        private set

    fun frameUpdate() {
        val time = System.nanoTime()
        if (lastFrameTime == -1L) {
            lastFrameTime = time
        }
        val deltaMs = (time - lastFrameTime) / 1_000_000f
        lastFrameTime = time

        if (mspfMinAcc < 0f || deltaMs < mspfMinAcc) {
            mspfMinAcc = deltaMs
        }
        if (mspfMaxAcc < 0f || deltaMs > mspfMaxAcc) {
            mspfMaxAcc = deltaMs
        }
        mspfAvgAcc += deltaMs

        lastAllocationRateB = allocationTracker.getBytesAllocatedPerSec()

        val frameCtrDiff = time - frameCounterStart
        if (frameCtrDiff >= 1_000_000_000L) {
            fps = frameAcc
            mspfAvg = mspfAvgAcc / frameAcc.coerceAtLeast(1)
            mspfMin = mspfMinAcc
            mspfMax = mspfMaxAcc

            mspfAvgAcc = 0f
            mspfMinAcc = -1f
            mspfMaxAcc = -1f
            frameAcc = 0
            frameCounterStart = time
        }
        frameAcc++
    }
}
