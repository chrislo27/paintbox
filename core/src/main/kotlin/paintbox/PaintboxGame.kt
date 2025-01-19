package paintbox

import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import paintbox.Paintbox.UIDebugOutlineMode.*
import paintbox.debug.DebugInfo
import paintbox.debug.DebugOverlay
import paintbox.font.FontCache
import paintbox.i18n.ILocalization
import paintbox.input.ExceptionHandlingInputMultiplexer
import paintbox.logging.SysOutPiper
import paintbox.registry.AssetRegistry
import paintbox.registry.ScreenRegistry
import paintbox.util.Version
import paintbox.util.WindowSize
import paintbox.util.gdxutils.GdxGame
import paintbox.util.gdxutils.isShiftDown
import kotlin.system.measureNanoTime

/**
 * This class is the base of all Paintbox games.
 *
 * [ResizeAction] and its other size parameters in [paintboxSettings]
 * are behaviours for how resizing works, which is is important for fonts that scale up with render size.
 */
abstract class PaintboxGame(val paintboxSettings: PaintboxSettings) : GdxGame(), InputProcessor {

    companion object {

        lateinit var fillTexture: Texture
            private set
        lateinit var spritesheetTexture: Texture
            private set
        lateinit var paintboxSpritesheet: PaintboxSpritesheet
            private set
        lateinit var gameInstance: PaintboxGame
            private set
        lateinit var launchArguments: List<String>
            private set
    }

    /**
     * Values are set by the `PaintboxDesktopLauncher`.
     */
    class LauncherSettings {

        /**
         * If not null, the max FPS was manually set and is a value value (0 or larger).
         */
        var fps: Int? = null

        /**
         * If not null, the VSync setting was manually set.
         */
        var vsync: Boolean? = null
    }

    init {
        @Suppress("RedundantCompanionReference")
        Companion.launchArguments = paintboxSettings.launchArguments
    }

    val version: Version = paintboxSettings.version
    val versionString: String = version.toString()
    val launcherSettings: LauncherSettings = LauncherSettings()

    val debugInfo: DebugInfo = DebugInfo()
    var debugOverlay: DebugOverlay = DebugOverlay(this)
        protected set
    lateinit var originalResolution: WindowSize
        private set

    /**
     * A camera that represents the emulated size by the [resizeAction][PaintboxSettings.resizeAction].
     */
    val emulatedCamera: OrthographicCamera = OrthographicCamera()

    /**
     * A camera that always represents the actual window size, clamped to the minimum window size.
     */
    val actualWindowSizeCamera: OrthographicCamera = OrthographicCamera()

    lateinit var fontCache: FontCache
        private set
    lateinit var defaultFonts: DefaultFonts
        private set
    lateinit var batch: SpriteBatch
        private set
    lateinit var shapeRenderer: ShapeRenderer
        private set

    open val inputMultiplexer: InputMultiplexer = ExceptionHandlingInputMultiplexer({ exceptionHandler(it) })

    private var shouldToggleDebugAfterPress = true
    private val disposeCalls: MutableList<Runnable> = mutableListOf()

    /**
     * Set this for default debug localization behaviours.
     */
    protected var reloadableLocalizationInstances: List<ILocalization> = emptyList()
    

    /**
     * Should include the version.
     */
    abstract fun getTitle(): String

    var programLaunchArguments: List<String> = emptyList()

    override fun create() {
        val logToFile = paintboxSettings.logToFile
        if (logToFile != null) {
            SysOutPiper.pipe(programLaunchArguments, this, logToFile)
        }
        Paintbox.LOGGER = paintboxSettings.logger
        gameInstance = this

        originalResolution = WindowSize(Gdx.graphics.width, Gdx.graphics.height)
        resetCameras()

        val pixmap: Pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(1f, 1f, 1f, 1f)
            fill()
        }
        fillTexture = Texture(pixmap)
        pixmap.dispose()
        spritesheetTexture = Texture(Gdx.files.internal("paintbox/paintbox_spritesheet_noborder.png"), true).apply {
            this.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
        paintboxSpritesheet = PaintboxSpritesheet(spritesheetTexture)

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()
        fontCache = FontCache(this)
        defaultFonts = DefaultFonts(this).apply { 
            registerDefaultFonts()
        }
        val fontLoadNano = measureNanoTime {
            fontCache.resizeAll(emulatedCamera.viewportWidth.toInt(), emulatedCamera.viewportHeight.toInt())
        }
        Paintbox.LOGGER.info("Initialized all ${fontCache.fonts.size} fonts in ${fontLoadNano / 1_000_000.0} ms")

        Gdx.input.inputProcessor = inputMultiplexer.apply {
            addProcessor(0, this@PaintboxGame)
        }
    }

    fun resetViewportToScreen() {
        HdpiUtils.glViewport(0, 0, Gdx.graphics.width, Gdx.graphics.height)
    }

    /**
     * This function handles camera updates and screen clearing.
     */
    open fun preRender() {
        emulatedCamera.update()
        batch.projectionMatrix = emulatedCamera.combined
        shapeRenderer.projectionMatrix = emulatedCamera.combined

        (screen as? PaintboxScreen)?.renderUpdate()

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClearDepthf(1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT or GL20.GL_DEPTH_BUFFER_BIT or (if (Gdx.graphics.bufferFormat.coverageSampling) GL20.GL_COVERAGE_BUFFER_BIT_NV else 0))
    }

    /**
     * This is called after the main [render] function is called. Default implementation is to do nothing.
     */
    open fun postRender() {
    }

    protected open fun onDebugChange(old: Boolean, new: Boolean) {
    }

    /**
     * The default render function. This calls [preRender], then `super.[render]`, then [postRender].
     * The debug overlay is also rendered at this time.
     */
    final override fun render() {
        try {
            debugInfo.frameUpdate()

            resetViewportToScreen()

            preRender()
            super.render()
            postRender()

            resetViewportToScreen()

            if (Paintbox.debugMode.get()) {
                val batch = this.batch
                val tmpMatrix = debugOverlay.tmpMatrix

                tmpMatrix.set(batch.projectionMatrix)
                batch.projectionMatrix = actualWindowSizeCamera.combined
                batch.begin()

                debugOverlay.render()

                batch.end()
                batch.projectionMatrix = tmpMatrix
            }
        } catch (t: Throwable) {
            t.printStackTrace()
            exceptionHandler(t)
        }
    }

    /**
     * Default exception handler is to exit the game.
     */
    protected open fun exceptionHandler(t: Throwable) {
        Gdx.app.exit()
    }

    /**
     * This returns a string to be put in the debug overlay above the current screen's debug string. By default this is
     * an empty string.
     */
    open fun getDebugString(): String {
        return ""
    }

    /**
     * This will reset the camera + reload fonts if the resize action is [ResizeAction.KEEP_ASPECT_RATIO]. It then calls
     * the super-method last.
     */
    override fun resize(width: Int, height: Int) {
        resetCameras()

        val actualWindowSizeCamera = actualWindowSizeCamera
        val nativeCamWidth = actualWindowSizeCamera.viewportWidth.toInt()
        val nativeCamHeight = actualWindowSizeCamera.viewportHeight.toInt()
        fontCache.resizeAll(nativeCamWidth, nativeCamHeight)
        
        super.resize(width, height)
    }

    override fun setScreen(screen: Screen?) {
        val current = getScreen()
        super.setScreen(screen)
        Paintbox.LOGGER.debug("Changed screens from ${current?.javaClass?.name} to ${screen?.javaClass?.name}")
    }

    override fun dispose() {
        Paintbox.LOGGER.info("Starting dispose call")

        super.dispose()

        disposeCalls.reversed().forEach { r ->
            try {
                r.run()
            } catch (t: Throwable) {
                t.printStackTrace()
            }
        }

        batch.dispose()
        shapeRenderer.dispose()
        fontCache.dispose()
        fillTexture.dispose()
        spritesheetTexture.dispose()

        ScreenRegistry.dispose()
        AssetRegistry.dispose()

        Paintbox.LOGGER.info("Dispose call finished, goodbye!")
    }

    fun addDisposeCall(runnable: Runnable) {
        disposeCalls += runnable
    }

    fun removeDisposeCall(runnable: Runnable) {
        disposeCalls -= runnable
    }

    fun getFPS(): Int = debugInfo.fps

    fun resetCameras() {
        val resizeAction = paintboxSettings.resizeAction
        val emulatedSize = paintboxSettings.emulatedSize
        val minimumSize = paintboxSettings.minimumSize

        val actualWindowSizeCamera = actualWindowSizeCamera
        actualWindowSizeCamera.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        if (actualWindowSizeCamera.viewportWidth < minimumSize.width || actualWindowSizeCamera.viewportHeight < minimumSize.height) {
            actualWindowSizeCamera.setToOrtho(false, minimumSize.width.toFloat(), minimumSize.height.toFloat())
        }
        actualWindowSizeCamera.update()

        val emulatedCamera = emulatedCamera
        when (resizeAction) {
            ResizeAction.ANY_SIZE -> emulatedCamera.setToOrtho(
                false, Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )

            ResizeAction.LOCKED -> emulatedCamera.setToOrtho(
                false, emulatedSize.width.toFloat(),
                emulatedSize.height.toFloat()
            )

            ResizeAction.KEEP_ASPECT_RATIO -> {
                val width: Float
                val height: Float

                if (Gdx.graphics.width < Gdx.graphics.height) {
                    width = Gdx.graphics.width.toFloat()
                    height = (emulatedSize.height.toFloat() / emulatedSize.width) * width
                } else {
                    height = Gdx.graphics.height.toFloat()
                    width = (emulatedSize.width.toFloat() / emulatedSize.height) * height
                }

                emulatedCamera.setToOrtho(false, width, height)
            }
        }
        if (emulatedCamera.viewportWidth < minimumSize.width || emulatedCamera.viewportHeight < minimumSize.height) {
            emulatedCamera.setToOrtho(false, minimumSize.width.toFloat(), minimumSize.height.toFloat())
        }
        emulatedCamera.update()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (Gdx.input.isKeyPressed(Paintbox.DEBUG_KEY)) {
            var pressed = true
            when (keycode) {
                Input.Keys.I -> {
                    val locs = reloadableLocalizationInstances.takeIf { it.isNotEmpty() }
                    if (locs != null) {
                        val nano = measureNanoTime {
                            locs.forEach { loc ->
                                loc.reloadAll()
                                loc.logMissingLocalizations(true)
                            }
                        }
                        Paintbox.LOGGER.debug(
                            "Reloaded I18N (${locs.size} instance(s)) from files in ${nano / 1_000_000.0} ms",
                            tag = "I18N"
                        )

                        val uniqueKeys: Set<String> = locs.flatMap { it.getAllUniqueKeysForAllLocales() }.toSet()
                        Paintbox.LOGGER.debug(
                            "Total of ${uniqueKeys.size} unique keys across all localization instances",
                            tag = "I18N"
                        )
                    } else {
                        Paintbox.LOGGER.debug(
                            "No I18N to reload, PaintboxGame#${::reloadableLocalizationInstances.name} was empty",
                            tag = "I18N"
                        )
                    }
                }

                Input.Keys.S -> {
                    val old = Paintbox.uiDebugOutlines.getOrCompute()
                    Paintbox.uiDebugOutlines.set(
                        when (old) {
                            NONE -> if (Gdx.input.isShiftDown()) ALL else ONLY_VISIBLE
                            ALL -> if (Gdx.input.isShiftDown()) ONLY_VISIBLE else NONE
                            ONLY_VISIBLE -> if (Gdx.input.isShiftDown()) ALL else NONE
                        }
                    )
                    Paintbox.LOGGER.debug("Toggled UI debug outlines to ${Paintbox.uiDebugOutlines}")
                }

                Input.Keys.G -> System.gc()
                else -> {
                    pressed = false
                }
            }
            if (shouldToggleDebugAfterPress && pressed) {
                shouldToggleDebugAfterPress = false
            }
            if (pressed) {
                return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Paintbox.DEBUG_KEY) {
            val shouldToggle = shouldToggleDebugAfterPress
            shouldToggleDebugAfterPress = true
            if (shouldToggle) {
                val old = Paintbox.debugMode.get()
                Paintbox.debugMode.set(!old)
                onDebugChange(old, !old)
                Paintbox.LOGGER.debug("Switched debug mode to ${!old}")
                return true
            }
        }
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun keyTyped(character: Char): Boolean {
        return false
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
    
//region Deprecations

    @Deprecated("Use actualWindowSizeCamera instead", ReplaceWith("actualWindowSizeCamera"))
    val nativeCamera: OrthographicCamera get() = actualWindowSizeCamera

//endregion
}
