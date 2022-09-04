package paintbox.util.gdxutils

import com.badlogic.gdx.Application
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.math.Interpolation


/**
 * Uses [Gdx.app][Application] `postRunnable` to transition a float value. [onUpdate] is called at least twice
 * throughout the lifetime of this object; once with currentValue = [startValue] and progress = 0.0, and at least once
 * with currentValue = [endValue] and progress = 1.0.
 * 
 * Create this [GdxRunnableTransition] and then call `Gdx.app.postRunnable` with this object as the [Runnable] to start.
 * The current `Gdx.app` and `Gdx.graphics` will be used on the first invocation of [run] from `postRunnable`.
 * 
 * Once this [GdxRunnableTransition] is used, it cannot be used again. The [copy] function can be used to create a new
 * one without retaining the state data.
 */
data class GdxRunnableTransition(
    val startValue: Float, val endValue: Float, val durationSec: Float,
    val interpolation: Interpolation = Interpolation.linear,
    val onUpdate: (currentValue: Float, progress: Float) -> Unit
) : Runnable {

    // INTENTIONAL: Do not put following properties in primary constructor, as these values are not to be copied!
    private var started: Boolean = false
    private lateinit var app: Application
    private lateinit var graphics: Graphics
    private var complete: Boolean = false
    private var timeElapsed: Float = 0f
    
    fun isStarted(): Boolean = started
    
    fun isCompleted(): Boolean = complete

    /**
     * Cancels this object and doesn't do any further updates.
     * @param setToEndValue If true, [onUpdate] will be called as if this transition finished. Otherwise nothing happens.
     */
    fun cancel(setToEndValue: Boolean) {
        if (!complete) {
            complete = true
            if (setToEndValue) {
                onUpdate(interpolation.apply(startValue, endValue, 1f), 1f)
            }
        }
    }

    override fun run() {
        if (this.complete) {
            return
        }
        
        if (!this.started) {
            this.started = true
            this.app = Gdx.app
            this.graphics = Gdx.graphics
            onUpdate(interpolation.apply(startValue, endValue, 0f), 0f)
        } else {
            timeElapsed += this.graphics.deltaTime
        }
        
        val progress: Float = if (durationSec <= 0f || !timeElapsed.isFinite()) {
            1f
        } else {
            (timeElapsed.coerceIn(0f, durationSec) / durationSec)
        }
        if (progress > 0f) {
            // Don't call again if progress == 0 since that's called in the not started block
            onUpdate(interpolation.apply(startValue, endValue, progress), progress)
        }
        
        if (progress >= 1f) {
            this.complete = true
        }
        
        if (!this.complete) {
            // Post this runnable again for the next frame update, if not yet complete
            this.app.postRunnable(this)
        }
    }
}
