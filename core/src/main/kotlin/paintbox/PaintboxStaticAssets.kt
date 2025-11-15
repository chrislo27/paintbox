package paintbox

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Disposable


/**
 * Must be instantiated on the gdx main thread.
 */
class PaintboxStaticAssets : Disposable {
    
    val fillTexture: Texture
    val paintboxSpritesheet: PaintboxSpritesheet

    init {
        val pixmap: Pixmap = Pixmap(1, 1, Pixmap.Format.RGBA8888).apply {
            setColor(1f, 1f, 1f, 1f)
            fill()
        }
        fillTexture = Texture(pixmap)
        pixmap.dispose()
        
        val spritesheetTexture = Texture(Gdx.files.internal("paintbox/paintbox_spritesheet_noborder.tga"), true).apply {
            this.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        }
        paintboxSpritesheet = PaintboxSpritesheet(spritesheetTexture, ownsTexture = true)
    }
    
    override fun dispose() {
        fillTexture.dispose()
        paintboxSpritesheet.dispose()
    }
}