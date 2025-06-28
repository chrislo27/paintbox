package paintbox.ui.animation

import paintbox.binding.FloatVar
import java.util.concurrent.ConcurrentHashMap


class AnimationHandler {

    private data class AnimationTuple(
        val animation: Animation, val varr: FloatVar, var accumulatedSeconds: Float, var alpha: Float = 0f,
    ) {

        private var onStartCalled: Boolean = false
        private var onCompleteCalled: Boolean = false

        fun callOnStart() {
            if (!onStartCalled) {
                onStartCalled = true
                animation.onStart?.invoke()
            }
        }

        fun callOnComplete() {
            if (!onCompleteCalled) {
                onCompleteCalled = true
                animation.onComplete?.invoke()
            }
        }
    }

    private val animations: MutableMap<FloatVar, AnimationTuple> = ConcurrentHashMap()

    /**
     * Set to 0 to disable animations.
     */
    val animationSpeed: FloatVar = FloatVar(1f)

    private val removeList: MutableSet<AnimationTuple> = mutableSetOf()

    fun frameUpdate(delta: Float) {
        val speed = animationSpeed.get()
        val isInstant = speed <= 0f

        animations.forEach { (_, tuple) ->
            val animation = tuple.animation
            tuple.accumulatedSeconds += delta

            if (tuple.accumulatedSeconds >= 0f) {
                tuple.callOnStart()

                val newAlpha = if (isInstant || animation.duration <= 0f) 1f
                else ((tuple.accumulatedSeconds * speed) / animation.duration).coerceIn(0f, 1f)
                tuple.alpha = newAlpha

                val newValue = tuple.animation.applyFunc(newAlpha)
                tuple.varr.set(newValue)

                if (newAlpha >= 1f) {
                    tuple.callOnComplete()
                    removeList.add(tuple)
                }
            }
        }

        if (removeList.isNotEmpty()) {
            removeList.forEach { tuple -> animations.remove(tuple.varr, tuple) }
            removeList.clear()
        }
    }

    fun enqueueAnimation(animation: Animation, varr: FloatVar) {
        val existing = animations.remove(varr)
        if (existing != null) {
            existing.varr.set(existing.animation.applyFunc(1f))
            existing.callOnComplete()
        }
        animations[varr] = AnimationTuple(animation, varr, -animation.delay)
    }

    fun cancelAnimation(animation: Animation) {
        val existing = animations.entries.firstOrNull { it.value.animation == animation } ?: return
        animations.remove(existing.key)
        existing.key.set(existing.value.animation.applyFunc(1f))
        existing.value.callOnComplete()
    }

    fun cancelAnimationFor(varr: FloatVar): Animation? {
        val existing = animations.remove(varr)
        if (existing != null) {
            existing.varr.set(existing.animation.applyFunc(1f))
            existing.callOnComplete()
        }
        return existing?.animation
    }
    
    fun cancelAllAnimations() {
        val floatVars = animations.keys.toList()
        floatVars.forEach { cancelAnimationFor(it) }
    }

}