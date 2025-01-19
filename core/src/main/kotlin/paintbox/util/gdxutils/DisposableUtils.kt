package paintbox.util.gdxutils

import com.badlogic.gdx.utils.Disposable


fun Disposable.disposeQuietly(printStackTrace: Boolean = false) {
    try {
        this.dispose()
    } catch (e: Exception) {
        if (printStackTrace) {
            e.printStackTrace()
        }
    }
}