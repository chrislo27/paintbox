package paintbox.ui


/**
 * A [Pane] whose [shouldExcludeFromInput] always returns true.
 */
open class NoInputPane : Pane() {
    final override fun shouldExcludeFromInput(): Boolean {
        return true
    }
}
