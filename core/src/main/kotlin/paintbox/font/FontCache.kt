package paintbox.font

import com.badlogic.gdx.utils.Disposable


open class FontCache : Disposable {

    val fonts: Map<Any, PaintboxFont> = mutableMapOf()

    operator fun get(key: Any): PaintboxFont {
        return fonts[key]
            ?: throw IllegalArgumentException("Font not found with key \"$key\" (key type ${key.javaClass.name})")
    }

    operator fun set(key: Any, font: PaintboxFont?) {
        if (font != null) {
            (fonts as MutableMap)[key] = font
        } else {
            val existing = fonts[key]
            if (existing != null) {
                (fonts as MutableMap).remove(key)
                existing.dispose()
            }
        }
    }

    fun resizeAll(width: Int, height: Int) {
        fonts.values.forEach {
            it.resize(width, height)
        }
    }

    override fun dispose() {
        fonts.values.forEach(Disposable::dispose)
    }
}