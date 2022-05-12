package paintbox.util.gdxutils

import com.badlogic.gdx.graphics.g2d.GlyphLayout.GlyphRun


fun GlyphRun.copy(): GlyphRun = GlyphRun().also { copy ->
    copy.x = this.x
    copy.y = this.y
    copy.width = this.width
    copy.glyphs.addAll(this.glyphs)
    copy.xAdvances.addAll(this.xAdvances)
}
