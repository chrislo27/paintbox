package paintbox.util.gdxutils


class GdxDelayedRunnable(val delaySec: Float, val action: () -> Unit) : Runnable {

    private val runnableTransition: GdxRunnableTransition

    private var isCancelled: Boolean = false
    private var isCompleted: Boolean = false

    init {
        runnableTransition = GdxRunnableTransition(0f, 1f, delaySec.coerceAtLeast(0f)) { _, progress ->
            if (progress >= 1f) {
                attemptCompletion()
            }
        }
    }

    fun isCancelled(): Boolean = isCancelled
    fun isCompleted(): Boolean = isCompleted

    fun cancel() {
        if (!isCancelled && !isCompleted) {
            isCancelled = true
        }
    }

    fun completeImmediately() {
        attemptCompletion()
    }

    private fun attemptCompletion() {
        if (!isCancelled && !isCompleted) {
            isCompleted = true
            action()
        }
    }

    override fun run() = runnableTransition.run()

}
