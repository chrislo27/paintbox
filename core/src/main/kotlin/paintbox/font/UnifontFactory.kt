package paintbox.font

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture.TextureFilter
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import paintbox.util.WindowSize


open class UnifontFactory {

    open fun getUnifontFileLocation(): FileHandle = Gdx.files.internal("paintbox/fonts/unifont/unifont.otf")

    open fun createUnifont(
        fontSize: Int = 16,
        borderWidth: Float = 0f,
        textureFilter: TextureFilter = TextureFilter.Linear,
    ): PaintboxFontFreeType {
        return PaintboxFontFreeType(
            createDefaultPaintboxFontParams(),
            createDefaultFreeTypeFontParameter(fontSize, borderWidth, textureFilter)
        )
    }

    protected open fun createDefaultPaintboxFontParams() = PaintboxFontParams(
        file = getUnifontFileLocation(),
        scaleToReferenceSize = false,
        referenceSize = WindowSize(1280, 720)
    )

    protected open fun createDefaultFreeTypeFontParameter(
        fontSize: Int,
        borderWidth: Float,
        textureFilter: TextureFilter,
    ): FreeTypeFontGenerator.FreeTypeFontParameter = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
        this.magFilter = textureFilter
        this.minFilter = textureFilter
        this.genMipMaps = false
        this.incremental = true
        this.mono = false
        this.color = Color(1f, 1f, 1f, 1f)
        this.borderColor = Color(0f, 0f, 0f, 1f)
        this.characters = ""
        this.hinting = FreeTypeFontGenerator.Hinting.Full
        this.size = fontSize
        this.borderWidth = borderWidth
    }
    
}