package paintbox.util.gdxutils


object GdxDelayedRunnable {

    inline fun create(delaySec: Float, crossinline action: () -> Unit): Runnable {
        return GdxRunnableTransition(0f, 1f, delaySec.coerceAtLeast(0f)) { _, progress ->
            if (progress >= 1f) {
                action()
            }
        }
    }

    inline operator fun invoke(delaySec: Float, crossinline action: () -> Unit): Runnable =
        create(delaySec, action)

}