package paintbox.util.wave


object WaveUtils {

    fun getSawtoothWave(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return SawtoothWave.getWaveValue(periodSec, timeMs, offsetMs)
    }

    fun getTriangleWave(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return TriangleWave.getWaveValue(periodSec, timeMs, offsetMs)
    }

    /**
     * The peaks of the sine wave will be returned as 1.0 and the troughs will be 0.0.
     * Starts at 0.5. Note that the [periodSec] argument indicates the period from centre to centre, which is only half a sine period.
     */
    fun getSineWave(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return SineWave.getWaveValue(periodSec * 2, timeMs, offsetMs)
    }

    /**
     * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
     * Starts at 1. Note that the [periodSec] argument indicates the period from peak to trough, which is only half a cosine period.
     */
    fun getCosineWave(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return CosineWave.getWaveValue(periodSec * 2, timeMs, offsetMs)
    }

    /**
     * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
     * Starts at 0. Note that the [periodSec] argument indicates the period from peak to trough, which is only half a cosine period.
     */
    fun getBaseCosineWave(periodSec: Float, timeMs: Long = System.currentTimeMillis(), offsetMs: Long = 0L): Float {
        return BaseCosineWave.getWaveValue(periodSec * 2, timeMs, offsetMs)
    }

}
