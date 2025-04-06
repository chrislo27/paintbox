package paintbox.input

import com.badlogic.gdx.*
import paintbox.binding.Var
import paintbox.prefs.PaintboxPreferences
import paintbox.util.MonitorInfo
import paintbox.util.WindowSize
import paintbox.util.gdxutils.isAltDown
import paintbox.util.gdxutils.isControlDown
import paintbox.util.gdxutils.isShiftDown


interface IFullscreenWindowedInputProcessor : InputProcessor {

    var blockResolutionChanges: Boolean

    fun attemptFullscreen()

    fun attemptEndFullscreen()

    fun attemptResetWindow()

}

abstract class FullscreenWindowedInputProcessor(
    private val defaultWindowSize: WindowSize,
) : InputAdapter(), IFullscreenWindowedInputProcessor {

    @Volatile
    override var blockResolutionChanges: Boolean = false

    protected var lastWindowed: WindowSize = defaultWindowSize.copy()

    abstract fun persistFullscreenMonitorToSettings(monitor: Graphics.Monitor)

    override fun attemptFullscreen() {
        val graphics = Gdx.graphics
        lastWindowed = WindowSize(graphics.width, graphics.height)
        graphics.setFullscreenMode(graphics.displayMode)

        val monitor = Gdx.graphics.monitor
        if (monitor != null) {
            persistFullscreenMonitorToSettings(monitor)
        }
    }

    override fun attemptEndFullscreen() {
        val last = lastWindowed
        Gdx.graphics.setWindowedMode(last.width, last.height)
    }

    override fun attemptResetWindow() {
        Gdx.graphics.setWindowedMode(defaultWindowSize.width, defaultWindowSize.height)
    }

    override fun keyDown(keycode: Int): Boolean {
        val processed = super.keyDown(keycode)
        if (!processed && !blockResolutionChanges) {
            if (handleFullscreenKeybinds(keycode)) {
                return true
            }
        }
        return processed
    }

    private fun handleFullscreenKeybinds(keycode: Int): Boolean {
        // Fullscreen shortcuts:
        // F11 - Toggle fullscreen
        // Alt+Enter - Toggle fullscreen
        // Shift+F11 - Reset window to default size
        val ctrl = Gdx.input.isControlDown()
        val shift = Gdx.input.isShiftDown()
        val alt = Gdx.input.isAltDown()
        if (!ctrl) {
            val currentlyFullscreen = Gdx.graphics.isFullscreen
            if (!alt && keycode == Input.Keys.F11) {
                if (!shift) {
                    if (currentlyFullscreen) {
                        attemptEndFullscreen()
                    } else {
                        attemptFullscreen()
                    }
                } else {
                    attemptResetWindow()
                }

                return true
            } else if (!shift && alt && keycode == Input.Keys.ENTER) {
                if (currentlyFullscreen) {
                    attemptEndFullscreen()
                } else {
                    attemptFullscreen()
                }

                return true
            }
        }

        return false
    }
}

class DefaultFullscreenWindowedInputProcessor<Prefs : PaintboxPreferences<*>>(
    defaultWindowSize: WindowSize,
    private val prefs: () -> Prefs,
    private val monitorVar: (Prefs) -> Var<MonitorInfo?>,
    private val windowedResolutionVar: (Prefs) -> Var<WindowSize>,
) : FullscreenWindowedInputProcessor(defaultWindowSize) {

    init {
        val originalLastWindowed = this.lastWindowed
        val persistedWindowRes = windowedResolutionVar(prefs()).getOrCompute()

        val displayMode = Gdx.graphics.displayMode
        if (displayMode != null && displayMode.width >= originalLastWindowed.width && displayMode.height >= originalLastWindowed.height) {
            this.lastWindowed = persistedWindowRes
        }
    }

    override fun persistFullscreenMonitorToSettings(monitor: Graphics.Monitor) {
        val settings = prefs()
        monitorVar(settings).set(MonitorInfo.fromMonitor(monitor))
        settings.persist()
    }
}
