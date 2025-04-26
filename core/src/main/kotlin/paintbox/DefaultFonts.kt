package paintbox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import paintbox.font.*
import paintbox.util.WindowSize


class DefaultFonts(private val paintboxGame: PaintboxGame) {

    private object FontKeys {

        const val UNIFONT: String = "UNIFONT"
        const val DEBUG_FONT: String = "DEBUG_FONT"
        const val DEBUG_FONT_BORDERED: String = "DEBUG_FONT_BORDERED"
        const val DEBUG_FONT_BOLD: String = "DEBUG_FONT_BOLD"
        const val DEBUG_FONT_BOLD_BORDERED: String = "DEBUG_FONT_BOLD_BORDERED"
        const val DEBUG_FONT_ITALIC: String = "DEBUG_FONT_ITALIC"
        const val DEBUG_FONT_ITALIC_BORDERED: String = "DEBUG_FONT_ITALIC_BORDERED"
        const val DEBUG_FONT_BOLD_ITALIC: String = "DEBUG_FONT_BOLD_ITALIC"
        const val DEBUG_FONT_BOLD_ITALIC_BORDERED: String = "DEBUG_FONT_BOLD_ITALIC_BORDERED"
    }

    private val fontCache: FontCache get() = paintboxGame.fontCache


    val unifontFont: PaintboxFont
        get() = fontCache[FontKeys.UNIFONT]
    val debugFont: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT]
    val debugFontBordered: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_BORDERED]
    val debugFontBold: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_BOLD]
    val debugFontBoldBordered: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_BOLD_BORDERED]
    val debugFontItalic: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_ITALIC]
    val debugFontItalicBordered: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_ITALIC_BORDERED]
    val debugFontBoldItalic: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_BOLD_ITALIC]
    val debugFontBoldItalicBordered: PaintboxFont
        get() = fontCache[FontKeys.DEBUG_FONT_BOLD_ITALIC_BORDERED]

    val debugMarkup: Markup by lazy {
        Markup.createWithBoldItalic(
            debugFont,
            debugFontBold,
            debugFontItalic,
            debugFontBoldItalic
        )
    }
    val debugMarkupBordered: Markup by lazy {
        Markup.createWithBoldItalic(
            debugFontBordered,
            debugFontBoldBordered,
            debugFontItalicBordered,
            debugFontBoldItalicBordered
        )
    }


    fun registerDefaultFonts() {
        fun makeFtfParam() = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            magFilter = Texture.TextureFilter.Linear
            minFilter = Texture.TextureFilter.Linear
            genMipMaps = false
            incremental = true
            mono = false
            color = Color(1f, 1f, 1f, 1f)
            borderColor = Color(0f, 0f, 0f, 1f)
            characters = ""
            hinting = FreeTypeFontGenerator.Hinting.Full
        }

        val defaultReferenceSize = WindowSize(1280, 720)
        fun createDefaultFontParams(filename: String): PaintboxFontParams {
            return PaintboxFontParams(
                file = Gdx.files.internal("paintbox/fonts/$filename"),
                scaleToReferenceSize = false,
                referenceSize = defaultReferenceSize
            )
        }

        val afterLoad: PaintboxFontFreeType.(font: BitmapFont) -> Unit = { font ->
            font.setFixedWidthGlyphs("1234567890")
            font.setUseIntegerPositions(false)
        }

        val defaultFontSize = 16
        val defaultBorderWidth = 1.5f
        val normalFilename = "OpenSans-Regular.ttf"
        val normalItalicFilename = "OpenSans-Italic.ttf"
        val boldFilename = "OpenSans-Bold.ttf"
        val boldItalicFilename = "OpenSans-BoldItalic.ttf"

        val cache = fontCache

        cache[FontKeys.DEBUG_FONT] = PaintboxFontFreeType(
            createDefaultFontParams(normalFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = 0f
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_BORDERED] = PaintboxFontFreeType(
            createDefaultFontParams(normalFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = defaultBorderWidth
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_ITALIC] = PaintboxFontFreeType(
            createDefaultFontParams(normalItalicFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = 0f
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_ITALIC_BORDERED] = PaintboxFontFreeType(
            createDefaultFontParams(normalItalicFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = defaultBorderWidth
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_BOLD] = PaintboxFontFreeType(
            createDefaultFontParams(boldFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = 0f
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_BOLD_BORDERED] = PaintboxFontFreeType(
            createDefaultFontParams(boldFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = defaultBorderWidth
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_BOLD_ITALIC] = PaintboxFontFreeType(
            createDefaultFontParams(boldItalicFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = 0f
            }).setAfterLoad(afterLoad)
        cache[FontKeys.DEBUG_FONT_BOLD_ITALIC_BORDERED] = PaintboxFontFreeType(
            createDefaultFontParams(boldItalicFilename),
            makeFtfParam().apply {
                size = defaultFontSize
                borderWidth = defaultBorderWidth
            }).setAfterLoad(afterLoad)

        cache[FontKeys.UNIFONT] = UnifontFactory().createUnifont(fontSize = 16)
    }

}