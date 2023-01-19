package paintbox.util.wave

import kotlin.math.absoluteValue


/**
 * Returns a value for a new wave where the first half (one [periodSec]) is [WaveFunction.getWaveValue], and the second
 * half is [WaveFunction.getInvertedValue].
 */
fun WaveFunction.getBoomerangValue(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
    if (periodSec == 0f) return getZeroPeriodValue()

    val isPeriodNegative = periodSec < 0f
    val absPeriod = periodSec.absoluteValue
    val sawtooth = SawtoothWave.getWaveValue(absPeriod * 2, timeMs, offsetMs)
    val regularValue = if (sawtooth >= 0.5f) getWaveValue(absPeriod, timeMs, offsetMs) else getInvertedValue(absPeriod, timeMs, offsetMs)

    return if (isPeriodNegative) (1f - regularValue) else regularValue
}

/**
 * Returns [WaveFunction.getWaveValue] with a new range, with 0.0 mapped to [zeroValue] and 1.0 mapped to [oneValue].
 */
fun WaveFunction.getMappedValue(zeroValue: Float, oneValue: Float, periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
    return zeroValue + (oneValue + zeroValue) * getWaveValue(periodSec, timeMs, offsetMs)
}
