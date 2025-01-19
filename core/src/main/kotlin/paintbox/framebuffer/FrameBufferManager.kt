package paintbox.framebuffer

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.graphics.glutils.HdpiUtils
import com.badlogic.gdx.utils.Disposable
import com.badlogic.gdx.utils.Scaling
import paintbox.Paintbox
import paintbox.PaintboxGame
import paintbox.util.WindowSize
import paintbox.util.gdxutils.NestedFrameBuffer
import paintbox.util.gdxutils.disposeQuietly
import kotlin.math.roundToInt


/**
 * A [FrameBufferManager] manages a set of [NestedFrameBuffer]s where they all have a size and ratio dictated by
 * [Scaling]. The buffers get automatically disposed and recreated whenever the window size changes.
 */
class FrameBufferManager(
    val bufferSettingsPerBuffer: List<BufferSettings>,
    loggerTag: String = "",
    val scaling: Scaling = Scaling.fit,
    val referenceWindowSize: WindowSize = DEFAULT_REFERENCE_WINDOW_SIZE,
) : Disposable {
    
    companion object {

        private const val LOGGER_TAG_PREFIX: String = "FrameBufferManager"
        val DEFAULT_REFERENCE_WINDOW_SIZE: WindowSize = WindowSize(1280, 720)
    }
    
    data class BufferSettings(
        val format: Pixmap.Format = Pixmap.Format.RGBA8888,
        val hasDepth: Boolean = true, val hasStencil: Boolean = false,
        val minFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
        val magFilter: Texture.TextureFilter = Texture.TextureFilter.Linear,
    )

    private val loggerTag: String = if (loggerTag == "") LOGGER_TAG_PREFIX else "$LOGGER_TAG_PREFIX: $loggerTag"
    private val numBuffers: Int = bufferSettingsPerBuffer.size
    private var isDisposed: Boolean = false

    private val framebuffers: Array<NestedFrameBuffer?> = Array(numBuffers) { null }
    private val fbTexRegs: Array<TextureRegion> = Array(numBuffers) { TextureRegion(PaintboxGame.gameInstance.staticAssets.fillTexture) }

    private var lastKnownWindowSize: WindowSize = WindowSize(-1, -1)
    private var framebufferSize: WindowSize = WindowSize(0, 0)

    constructor(
        numBuffers: Int, commonBufferSettings: BufferSettings,
        loggerTag: String = "",
        scaling: Scaling = Scaling.fit,
        referenceWindowSize: WindowSize = DEFAULT_REFERENCE_WINDOW_SIZE,
    ) : this(List(numBuffers) { commonBufferSettings }, loggerTag, scaling, referenceWindowSize)


    fun getFramebuffer(index: Int): NestedFrameBuffer? = framebuffers.getOrNull(index)
    
    fun getFramebufferRegion(index: Int): TextureRegion = fbTexRegs[index]

    /**
     * This should be called once per frame at the start of the render.
     * This may update the frame buffers inside this manager as a result.
     *
     * MUST be called during rendering and not in Gdx.postRunnable.
     *
     * If any dimension of the current window size is 0 (possible if the window is minimized),
     * then the [referenceWindowSize] is used as the buffer size.
     *
     * @return True if framebuffers were recreated
     */
    fun frameUpdate(): Boolean {
        val cachedWindowSize = this.lastKnownWindowSize
        val nullWindowSize = cachedWindowSize.width == -1 || cachedWindowSize.height == -1
        val windowWidth = Gdx.graphics.width
        val windowHeight = Gdx.graphics.height
        if (nullWindowSize || ((windowWidth > 0 && windowHeight > 0) && (windowWidth != cachedWindowSize.width || windowHeight != cachedWindowSize.height))) {
            // Width and/or height MAY be 0.
            this.lastKnownWindowSize = WindowSize(windowWidth, windowHeight)

            val vpWidth: Int
            val vpHeight: Int
            this.scaling.apply(
                referenceWindowSize.width.toFloat(),
                referenceWindowSize.height.toFloat(),
                windowWidth.toFloat(),
                windowHeight.toFloat()
            ).let { worldSize ->
                vpWidth = worldSize.x.roundToInt()
                vpHeight = worldSize.y.roundToInt()
            }

            val cachedFramebufferSize = this.framebufferSize
            if (vpWidth > 0 && vpHeight > 0 && (cachedFramebufferSize.width != vpWidth || cachedFramebufferSize.height != vpHeight)) {
                createFramebuffers(vpWidth, vpHeight)
            } else if (nullWindowSize) {
                createFramebuffers(referenceWindowSize.width, referenceWindowSize.height)
            } else {
                return false
            }

            return true
        }

        return false
    }

    private fun disposeFramebuffers() {
        val framebuffersArray = this.framebuffers
        val oldFbsList = framebuffersArray.toList()
        framebuffersArray.fill(null)

        var numDisposed = 0
        oldFbsList.forEachIndexed { i, fb ->
            if (fb != null) {
                fb.disposeQuietly()
                fbTexRegs[i].texture = null
                numDisposed++
            }
        }
        if (numDisposed > 0) {
            Paintbox.LOGGER.debug("Disposed of $numDisposed framebuffers", tag = loggerTag)
        }
    }

    private fun createFramebuffers(width: Int, height: Int) {
        val framebuffersArray = this.framebuffers
        disposeFramebuffers()

        if (!this.isDisposed) {
            // fb width/height has to be at least 1. It can return 0 if the window width and/or height is 0 (minimized)
            val newFbWidth = HdpiUtils.toBackBufferX(width).coerceAtLeast(1)
            val newFbHeight = HdpiUtils.toBackBufferY(height).coerceAtLeast(1)
            framebuffersArray.indices.forEach { i ->
                val settings = bufferSettingsPerBuffer[i]
                val newFb = NestedFrameBuffer(
                    settings.format,
                    newFbWidth,
                    newFbHeight,
                    settings.hasDepth,
                    settings.hasStencil
                ).apply {
                    this.colorBufferTexture.setFilter(settings.minFilter, settings.magFilter)
                }
                framebuffersArray[i] = newFb
                fbTexRegs[i].also { tr ->
                    tr.setRegion(newFb.colorBufferTexture)
                    tr.flip(false, true)
                }
            }
            this.framebufferSize = WindowSize(newFbWidth, newFbHeight)
            Paintbox.LOGGER.debug(
                "Re-created $numBuffers framebuffers to be backbuffer ${newFbWidth}x${newFbHeight} (logical ${width}x${height})",
                tag = loggerTag
            )
        }
    }

    override fun dispose() {
        if (!isDisposed) {
            disposeFramebuffers()
            isDisposed = true
        }
    }
}
