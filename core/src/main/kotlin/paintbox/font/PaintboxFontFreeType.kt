package paintbox.font

import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import paintbox.binding.LongVar
import paintbox.binding.ReadOnlyLongVar
import paintbox.util.WindowSize
import paintbox.util.gdxutils.copy
import kotlin.math.max
import kotlin.math.min

typealias FreeTypeFontAfterLoad = PaintboxFontFreeType.(BitmapFont) -> Unit

/**
 * A wrapper around a [FreeTypeFontGenerator].
 */
open class PaintboxFontFreeType(
    params: PaintboxFontParams,
    val ftfParameter: FreeTypeFontGenerator.FreeTypeFontParameter,
) : PaintboxFont(params) {

    companion object {
        init {
            FreeTypeFontGenerator.setMaxTextureSize(2048)
        }
    }

    private var isInBegin: Boolean = false
    private var isLoaded: Boolean = false

    private var currentFontNum: Long = Long.MIN_VALUE + 1
    override val currentFontNumber: Long get() = currentFontNum
    override val currentFontNumberVar: ReadOnlyLongVar = LongVar(currentFontNum)

    private var lastWindowSize: WindowSize = WindowSize(1280, 720)
    private var currentRenderedScale: Float = -1f
    var additionalRenderScale: Float = 1f

    private var generator: FreeTypeFontGenerator? = null
        set(value) {
            field?.dispose()
            field = value
        }
    private var currentBitmapFont: BitmapFont? = null
        set(value) {
            if (value !== field) {
                currentFontNum++
                (currentFontNumberVar as LongVar).set(currentFontNum)
            }
            field = value
        }

    private var afterLoad: FreeTypeFontAfterLoad = {}

    override fun begin(areaWidth: Float, areaHeight: Float): BitmapFont {
        if (isInBegin) {
            if (LENIENT_BEGIN_END) {
                end()
            } else {
                error("Cannot call begin before end")
            }
        }
        isInBegin = true

        if (!isLoaded || currentBitmapFont == null) {
            // If the loadPriority is ALWAYS, this will already have been loaded in resize()
            load()
        }

        val params = this.params.getOrCompute()
        if (params.scaleToReferenceSize) {
            val font = currentBitmapFont!!
            val referenceSize = params.referenceSize
            val scaleX = (referenceSize.width / areaWidth)
            val scaleY = (referenceSize.height / areaHeight)
            val scale = max(scaleX, scaleY) / additionalRenderScale
            if (scaleX >= 0f && scaleY >= 0f && scaleX.isFinite() && scaleY.isFinite()) {
                font.data.setScale(scale, scale)
            }
        }

        return currentBitmapFont!!
    }

    override fun end() {
        if (!isInBegin && !LENIENT_BEGIN_END) error("Cannot call end before begin")
        isInBegin = false

        // Sets font back to scaleXY = 1.0
        val currentFont = this.currentBitmapFont
        if (currentFont != null) {
            this.fontDataInfo.applyToFont(currentFont)
        }
    }

    override fun resize(width: Int, height: Int) {
        this.dispose()
        lastWindowSize = WindowSize(width, height)
        val params = this.params.getOrCompute()
        if (params.loadPriority == PaintboxFontParams.LoadPriority.ALWAYS) {
            load()
        }
    }

    private fun load() {
        val windowSize = this.lastWindowSize
        val params = this.params.getOrCompute()
        val referenceSize = params.referenceSize
        val scale: Float = (if (!params.scaleToReferenceSize) 1f else min(
            windowSize.width.toFloat() / referenceSize.width,
            windowSize.height.toFloat() / referenceSize.height
        )) * additionalRenderScale

        if (this.currentRenderedScale != scale) {
            this.dispose()
            this.currentRenderedScale = scale

            val oldFtfParam = this.ftfParameter
            val newFtfParam = this.ftfParameter.copy()
            newFtfParam.size = (oldFtfParam.size * scale).toInt()
            newFtfParam.borderWidth = oldFtfParam.borderWidth * scale

            val generator: FreeTypeFontGenerator = FreeTypeFontGeneratorFix(params.file)
            val generatedBitmapFont = generator.generateFont(newFtfParam)
            this.generator = generator
            this.currentBitmapFont = generatedBitmapFont

            this.afterLoad(generatedBitmapFont)
            this.fontDataInfo.copyFromFont(generatedBitmapFont)
        }

        this.isLoaded = true
    }

    fun setAfterLoad(func: FreeTypeFontAfterLoad): PaintboxFontFreeType {
        afterLoad = func
        return this
    }

    @Synchronized
    override fun dispose() {
        this.isLoaded = false
    }

    override fun onParamsChanged() {
        this.isLoaded = false
    }
}
