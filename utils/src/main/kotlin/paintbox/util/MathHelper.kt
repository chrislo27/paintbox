package paintbox.util

import paintbox.util.wave.WaveUtils
import kotlin.math.*


/**
 * An assortment of math utilities.
 */
object MathHelper {

    fun isPointIn(px: Float, py: Float, x: Float, y: Float, w: Float, h: Float): Boolean {
        // the following is to normalize negative widths and heights
        val width = if (w < 0) abs(w) else w
        val height = if (h < 0) abs(h) else h
        val realX = if (w < 0) x - width else x
        val realY = if (h < 0) y - height else y

        return px in realX..(realX + width) && py in realY..(realY + height)
    }

    fun isIntersecting(x1: Float, y1: Float, w1: Float, h1: Float, x2: Float, y2: Float, w2: Float,
                       h2: Float): Boolean {
        val width1 = if (w1 < 0) abs(w1) else w1
        val height1 = if (h1 < 0) abs(h1) else h1
        val realX1 = if (w1 < 0) x1 - width1 else x1
        val realY1 = if (h1 < 0) y1 - height1 else y1
        val width2 = if (w2 < 0) abs(w2) else w2
        val height2 = if (h2 < 0) abs(h2) else h2
        val realX2 = if (w2 < 0) x2 - width2 else x2
        val realY2 = if (h2 < 0) y2 - height2 else y2

        return (realX1 in realX2..(realX2 + width2) || (realX1 + width1) in realX2..(realX2 + width2))
                && (realY1 in realY2..(realY2 + height2) || (realY2 + height2) in realY2..(realY2 + height2))
    }

    fun snapToNearest(value: Float, interval: Float): Float {
        val abs = abs(interval)
        if (abs <= 0f)
            return value
        return (value / abs).roundToInt() * abs
    }

    fun snapToNearest(value: Double, interval: Double): Double {
        val abs = abs(interval)
        if (abs <= 0.0)
            return value
        return (value / abs).roundToLong() * abs
    }

    @Deprecated("Use WaveUtils instead")
    fun getSawtoothWave(time: Long, seconds: Float): Float = WaveUtils.getSawtoothWave(seconds, time)

    @Deprecated("Use WaveUtils instead")
    fun getSawtoothWave(seconds: Float): Float = WaveUtils.getSawtoothWave(seconds)

    @Deprecated("Use WaveUtils instead")
    fun getTriangleWave(ms: Long, seconds: Float): Float = WaveUtils.getTriangleWave(seconds, ms)

    @Deprecated("Use WaveUtils instead")
    fun getTriangleWave(sec: Float): Float = WaveUtils.getTriangleWave(sec)


    @Deprecated("Use WaveUtils instead")
    fun getSineWave(ms: Long, seconds: Float): Float = WaveUtils.getSineWave(seconds, ms)

    @Deprecated("Use WaveUtils instead")
    fun getSineWave(sec: Float): Float = WaveUtils.getSineWave(sec)

    /**
     * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
     * Starts at 1. Note that the [seconds] argument indicates the period from peak to trough.
     */
    @Deprecated("Use WaveUtils instead")
    fun getCosineWave(ms: Long, seconds: Float): Float = WaveUtils.getCosineWave(seconds, ms)

    @Deprecated("Use WaveUtils instead")
    fun getCosineWave(sec: Float): Float = WaveUtils.getCosineWave(sec)

    /**
     * The peaks of the cosine wave will be returned as 1.0 and the troughs will be 0.0.
     * Starts at 0. Note that the [seconds] argument indicates the period from peak to trough.
     */
    @Deprecated("Use WaveUtils instead")
    fun getBaseCosineWave(ms: Long, seconds: Float): Float = WaveUtils.getBaseCosineWave(seconds, ms)

    @Deprecated("Use WaveUtils instead")
    fun getBaseCosineWave(sec: Float): Float = WaveUtils.getBaseCosineWave(sec)
    
}
