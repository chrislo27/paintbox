package paintbox.prefs

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Graphics
import com.badlogic.gdx.Preferences
import com.badlogic.gdx.utils.Disposable
import paintbox.Paintbox
import paintbox.PaintboxGame
import paintbox.binding.Var
import paintbox.util.MonitorInfo
import paintbox.util.Version
import paintbox.util.WindowSize


@Suppress("MemberVisibilityCanBePrivate")
abstract class PaintboxPreferences<Game : PaintboxGame>(val game: Game, val prefs: Preferences) : Disposable {

    companion object {

        fun determineMaxRefreshRate(minimum: Int = 24, fallback: Int = 60): Int {
            return try {
                Gdx.graphics.displayMode.refreshRate.coerceAtLeast(minimum)
            } catch (e: Exception) {
                Paintbox.LOGGER.warn("Failed to detect refresh rate for current display mode")
                e.printStackTrace()
                fallback
            }
        }

        private fun Preferences.getNewIndicator(newIndicator: NewIndicator, lastLoadedVersion: Version?) {
            val containsKey = this.contains(newIndicator.key)
            val defaultValue: Boolean = when {
                lastLoadedVersion == null -> newIndicator.newEvenIfFirstPlay
                lastLoadedVersion <= newIndicator.newAsOf -> true
                else -> false
            }
            if (containsKey) {
                newIndicator.value.set(this.getBoolean(newIndicator.key, defaultValue))
            } else {
                newIndicator.value.set(defaultValue)
                this.putBoolean(newIndicator.key, defaultValue)
            }
        }

        private fun Preferences.putNewIndicator(newIndicator: NewIndicator) {
            this.putBoolean(newIndicator.key, newIndicator.value.get())
        }
    }

    protected class InitScope {

        val allKeyValues: MutableList<KeyValue<*>> = mutableListOf()
        val allNewIndicators: MutableList<NewIndicator> = mutableListOf()

        fun <KV : KeyValue<T>, T> KV.add(): KV {
            allKeyValues += this
            return this
        }

        fun NewIndicator.add(): NewIndicator {
            allNewIndicators += this
            return this
        }
    }

    var lastVersion: Version? = null
        private set

    protected abstract val allKeyValues: List<KeyValue<*>>
    abstract val allNewIndicators: List<NewIndicator>

    abstract fun getLastVersionKey(): String

    open fun load() {
        val prefs = this.prefs

        allKeyValues.forEach { kv ->
            kv.load(prefs)
        }
        val lastVersion: Version? = Version.parse(prefs.getString(getLastVersionKey()) ?: "")
        this.lastVersion = lastVersion
        allNewIndicators.forEach { prefs.getNewIndicator(it, lastVersion) }
    }

    open fun persist() {
        val prefs = this.prefs

        allKeyValues.forEach { kv ->
            kv.persist(prefs)
        }
        allNewIndicators.forEach { prefs.putNewIndicator(it) }

        prefs.flush()
    }

    /**
     * Can be called by the implementation of [PaintboxGame] when initializing this instance.
     * This should be overridden by the implementation of [PaintboxPreferences].
     */
    open fun setStartupSettings() {
        // Call setFpsAndVsync in impl...
        // Call setFullscreenOrWindowed in impl...
    }

    protected open fun setFpsAndVsync(maxFramerate: Var<Int>, vsyncEnabled: Var<Boolean>) {
        // LauncherSettings override properties
        val fps = game.launcherSettings.fps
        if (fps != null) {
            maxFramerate.set(fps.coerceAtLeast(0))
        }
        val vsync = game.launcherSettings.vsync
        if (vsync != null) {
            vsyncEnabled.set(vsync)
        }
        Gdx.app.postRunnable {
            val gr = Gdx.graphics
            gr.setForegroundFPS(maxFramerate.getOrCompute())
            gr.setVSync(vsyncEnabled.getOrCompute())
        }
    }

    protected open fun setFullscreenOrWindowed(fullscreen: Var<Boolean>, fullscreenMonitor: Var<MonitorInfo?>, windowedResolution: Var<WindowSize>) {
        val graphics = Gdx.graphics
        if (fullscreen.getOrCompute()) {
            val monitorInfo: MonitorInfo? = fullscreenMonitor.getOrCompute()
            if (monitorInfo == null) {
                // Use default display mode
                graphics.setFullscreenMode(graphics.displayMode)
            } else {
                val monitors = graphics.monitors
                // Search in order of: exact match, primary monitor if name matches, any monitor matches the name, then current monitor
                val monitor: Graphics.Monitor = monitors.firstOrNull(monitorInfo::doesMonitorMatch)
                    ?: graphics.primaryMonitor.takeIf { it.name == monitorInfo.name }
                    ?: monitors.firstOrNull { it.name == monitorInfo.name }
                    ?: graphics.monitor
                val displayMode: Graphics.DisplayMode = graphics.getDisplayMode(monitor) ?: graphics.displayMode
                graphics.setFullscreenMode(displayMode)
            }
        } else {
            val res = windowedResolution.getOrCompute()
            if (graphics.width != res.width || graphics.height != res.height) {
                graphics.setWindowedMode(res.width, res.height)
            }
        }
    }

    override fun dispose() {
        prefs.putString(getLastVersionKey(), game.version.toString()).flush()
        persist()
    }

}
