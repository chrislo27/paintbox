package paintbox.font

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import paintbox.util.WindowSize


class PaintboxFontUnifont
    : PaintboxFontFreeType(PaintboxFontParams(Gdx.files.internal("paintbox/fonts/unifont/unifont.otf"), 16, 0f,
        false, WindowSize(1280, 720)), FreeTypeFontGenerator.FreeTypeFontParameter().apply {
    magFilter = Texture.TextureFilter.Linear
    minFilter = Texture.TextureFilter.Linear
    genMipMaps = false
    incremental = true
    mono = false
    color = Color(1f, 1f, 1f, 1f)
    borderColor = Color(0f, 0f, 0f, 1f)
    characters = ""
    hinting = FreeTypeFontGenerator.Hinting.Full
    size = 16
})
