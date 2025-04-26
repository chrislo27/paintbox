package paintbox.font

import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.PixmapPacker
import com.badlogic.gdx.graphics.g2d.freetype.FreeType
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import kotlin.math.roundToInt


class FreeTypeFontGeneratorBaseLineRoundingFix : FreeTypeFontGenerator {
    
    constructor(fontFile: FileHandle) : super(fontFile)

    constructor(fontFile: FileHandle, faceIndex: Int) : super(fontFile, faceIndex)

    /**
     * `baseLine` can differ due to floating point error even among glyphs. This is most common
     * when setting the default characters or fixed-width glyphs.
     * However, in `super.createGlyph`, `baseLine` is always _cast_ to an int. This can cause
     * off-by-one issues (example: 26.0 vs 25.999998). Here, it is preemptively rounded to the nearest integer
     * when passed to the super implementation.
     */
    override fun createGlyph(
        c: Char,
        data: FreeTypeBitmapFontData?,
        parameter: FreeTypeFontParameter?,
        stroker: FreeType.Stroker?,
        baseLine: Float,
        packer: PixmapPacker?,
    ): BitmapFont.Glyph? {
        return super.createGlyph(c, data, parameter, stroker, baseLine.roundToInt().toFloat(), packer)
    }
}