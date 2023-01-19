package paintbox.util.wave

import kotlin.math.absoluteValue


abstract class WaveFunction {

    /**
     * Internal implementation. The implementor should assume that [absolutePeriodSec] is always greater than zero.
     * 
     * @return A value between 0.0 and 1.0 inclusive.
     */
    protected abstract fun generateValue(absolutePeriodSec: Float, timeMs: Long, offsetMs: Long): Float

    /**
     * The value to return if the provided period was zero.
     */
    open fun getZeroPeriodValue(): Float = 0f

    /**
     * Returns a value between 0.0 and 1.0 based on the [timeMs] and [periodSec]. 
     * This function is guaranteed to repeat every [periodSec] seconds.
     * 
     * If [periodSec] is equal to zero, this function returns an arbitrary fixed value.
     * If [periodSec] is negative, then this function behaves like [getInvertedValue] with an absolute value period.
     * 
     * @param periodSec The number of seconds before this function repeats.
     * @param timeMs The input time. Should be positive. Recommended to use [System.currentTimeMillis].
     * @param offsetMs The offset in milliseconds. May be positive or negative.
     */
    fun getWaveValue(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return when {
            periodSec > 0f -> generateValue(periodSec, timeMs, offsetMs)
            periodSec < 0f -> 1f - generateValue(periodSec.absoluteValue, timeMs, offsetMs)
            else -> getZeroPeriodValue()
        }
    }

    /**
     * Returns 1.0 - [getWaveValue].
     */
    fun getInvertedValue(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return 1f - getWaveValue(periodSec, timeMs, offsetMs)
    }
    
}
