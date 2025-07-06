package paintbox.tests

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.HdpiMode
import com.beust.jcommander.JCommander
import paintbox.PaintboxGame
import paintbox.ResizeAction
import paintbox.desktop.PaintboxArguments
import paintbox.desktop.PaintboxDesktopLauncher
import paintbox.logging.Logger
import paintbox.tests.newui.NewUITestGame
import paintbox.tests.newui.ScaledFontTestGame
import paintbox.tests.newui.UIAnchorTestEditorScreen
import paintbox.tests.newui.UITextLabelAlignTestScreen
import paintbox.tests.textblocks.TextBlockTestGame
import paintbox.util.Version
import paintbox.util.WindowSize


internal object TestDesktopLauncher {

    @Suppress("UNUSED_VARIABLE")
    @JvmStatic
    fun main(args: Array<String>) {
        // https://github.com/chrislo27/RhythmHeavenRemixEditor/issues/273
        System.setProperty("jna.nosys", "true")

        val arguments = PaintboxArguments()
        val jcommander = JCommander.newBuilder().acceptUnknownOptions(false).addObject(arguments).build()
        jcommander.parse(*args)

        if (arguments.printHelp) {
            println(StringBuilder().apply { jcommander.usageFormatter.usage(this) })
            return
        }

        fun getDefaultLauncher(app: PaintboxGame): PaintboxDesktopLauncher {
            return PaintboxDesktopLauncher(app, arguments).editConfig {
                this.setAutoIconify(true)
                val emulatedSize = app.paintboxSettings.emulatedSize
                this.setWindowedMode(emulatedSize.width, emulatedSize.height)
                this.setTitle(app.getWindowTitle())
                this.setResizable(true)
                this.useVsync(true)
                this.setForegroundFPS(60)
                this.setIdleFPS(60)
                this.setInitialBackgroundColor(Color(0f, 0f, 0f, 1f))
                this.setAudioConfig(100, 4096, 16)
                this.setHdpiMode(HdpiMode.Logical)
            }
        }

        val logger = Logger()
        val settings = TestPaintboxSettings(
            args.toList(), logger, null, Version(0, 1, 0),
            WindowSize(1280, 720), ResizeAction.ANY_SIZE, WindowSize(800, 450)
        )
        val test1: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> by lazy {
            TestGame(settings) to {}
        }
        val test2: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> by lazy {
            TextBlockTestGame(settings) to {}
        }
        val test3: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> by lazy {
            NewUITestGame(settings) { UIAnchorTestEditorScreen(it) } to {}
        }
        val test4: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> by lazy {
            ScaledFontTestGame(settings) to {}
        }
        val test5: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> by lazy {
            NewUITestGame(settings) { UITextLabelAlignTestScreen(it) } to {}
        }

        val selectedTest: Pair<PaintboxGame, (PaintboxDesktopLauncher) -> Unit> = test3
        getDefaultLauncher(selectedTest.first).apply {
            selectedTest.second.invoke(this)
        }.launch()
    }
}