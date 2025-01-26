package paintbox.desktop

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration
import paintbox.PaintboxGame
import paintbox.util.CloseListener
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * The launcher to use for desktop applications.
 * The system property `file.encoding` is set to `UTF-8`.
 */
class PaintboxDesktopLauncher(val game: PaintboxGame, val arguments: PaintboxArguments) {

    val config: Lwjgl3ApplicationConfiguration = Lwjgl3ApplicationConfiguration()

    init {
        System.setProperty("file.encoding", "UTF-8")
        System.setProperty("jna.nosys", "true")

        val fps = arguments.fps
        if (fps != null && fps >= 0) {
            config.setForegroundFPS(fps)
            config.setIdleFPS(if (fps == 0 /* fps = 0 -> unbounded*/) 60 else fps.coerceAtMost(60))
            game.launcherSettings.fps = fps
        }
        val vsync = arguments.vsync
        if (vsync != null) {
            config.useVsync(vsync)
            game.launcherSettings.vsync = vsync
        }
    }

    @OptIn(ExperimentalContracts::class)
    inline fun editConfig(func: Lwjgl3ApplicationConfiguration.() -> Unit): PaintboxDesktopLauncher {
        contract {
            callsInPlace(func, InvocationKind.EXACTLY_ONCE)
        }
        config.func()
        return this
    }

    fun launch(): Lwjgl3Application {
        val app = object : Lwjgl3Application(game, config) {
            override fun exit() {
                if ((game as? CloseListener)?.attemptClose() != false) {
                    super.exit()
                }
            }
        }
        return app
    }

}
