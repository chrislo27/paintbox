package paintbox.util.wave

import kotlin.math.roundToInt
import kotlin.math.sin


/**
 * Returns a increasing sawtooth wave, going from 0.0 to 1.0 linearly, then looping back to 0.0.
 *
 * Defaults to 0.
 */
object SawtoothWave : WaveFunction() {

    override fun generateValue(absolutePeriodSec: Float, timeMs: Long, offsetMs: Long): Float {
        return (timeMs + offsetMs) % (absolutePeriodSec * 1000).roundToInt() / (absolutePeriodSec * 1000f)
    }
}

object TriangleWave : WaveFunction() {

    override fun generateValue(absolutePeriodSec: Float, timeMs: Long, offsetMs: Long): Float {
        return SawtoothWave.getBoomerangValue(absolutePeriodSec / 2f, timeMs, offsetMs)
    }
}

open class SinusoidalWave(val thetaOffset: Float) : WaveFunction() {

    private val zeroPeriodValue: Float = this.generateValue(1f, 0L, 0L) // At time = 0, the period doesn't matter.

    override fun generateValue(absolutePeriodSec: Float, timeMs: Long, offsetMs: Long): Float {
        return 0.5f + 0.5f * sin((Math.PI / (absolutePeriodSec * 0.5f)) * ((timeMs + offsetMs) / 1000.0) + thetaOffset).toFloat()
    }

    override fun getZeroPeriodValue(): Float = zeroPeriodValue
}

/**
 * The peaks of the sine wave will be returned as 1.0 and the troughs will be 0.0.
 * Starts at 0.5.
 */
object SineWave : SinusoidalWave(0f)

/**
 * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
 * Starts at 1.
 */
object CosineWave : SinusoidalWave((Math.PI * 0.5).toFloat()) // Trig identity: cos(x) = sin(x + pi/2)

/**
 * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
 * Starts at 0.
 */
object BaseCosineWave : SinusoidalWave((Math.PI * 1.5).toFloat()) // Trig identity: -cos(x) = sin(x + pi*3/2)
