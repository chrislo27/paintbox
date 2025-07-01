package paintbox.ui

import java.util.concurrent.CopyOnWriteArraySet

/**
 * Used to listen to all [Focusable] changes.
 *
 * This is primarily to listen for when a text field gains focus in ANY [SceneRoot], to
 * trigger an on-screen keyboard (implementation dependent).
 */
object GlobalFocusableListeners {

    private val listeners: MutableSet<Listener> = CopyOnWriteArraySet()

    fun onFocusGained(sceneRoot: SceneRoot, focusable: Focusable) {
        listeners.forEach { it.onFocusGained(sceneRoot, focusable) }
    }

    fun onFocusLost(sceneRoot: SceneRoot, focusable: Focusable) {
        listeners.forEach { it.onFocusLost(sceneRoot, focusable) }
    }

    fun addListener(listener: Listener) {
        this.listeners += listener
    }

    fun removeListener(listener: Listener) {
        this.listeners -= listener
    }

    interface Listener {

        fun onFocusGained(sceneRoot: SceneRoot, focusable: Focusable)

        fun onFocusLost(sceneRoot: SceneRoot, focusable: Focusable)

    }

}