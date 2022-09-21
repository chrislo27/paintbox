package paintbox

import com.badlogic.gdx.Input
import paintbox.binding.BooleanVar
import paintbox.binding.Var
import paintbox.logging.Logger

/**
 * Holds constants and some info about Paintbox.
 */
object Paintbox {

    @Volatile
    lateinit var LOGGER: Logger

    const val DEBUG_KEY: Int = Input.Keys.F8
    val DEBUG_KEY_NAME: String = Input.Keys.toString(DEBUG_KEY)
    val debugMode: BooleanVar = BooleanVar(false)
    var uiDebugOutlines: Var<UIDebugOutlineMode> = Var(UIDebugOutlineMode.NONE)

    enum class UIDebugOutlineMode {
        NONE, ALL, ONLY_VISIBLE
    }

}