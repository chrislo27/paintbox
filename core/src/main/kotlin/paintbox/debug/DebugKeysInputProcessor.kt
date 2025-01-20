package paintbox.debug

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputAdapter
import com.badlogic.gdx.InputProcessor
import paintbox.Paintbox
import paintbox.Paintbox.UIDebugOutlineMode.*
import paintbox.i18n.ILocalization
import paintbox.input.ToggleableInputProcessor
import paintbox.util.gdxutils.isShiftDown
import kotlin.system.measureNanoTime


interface IDebugKeysInputProcessor : InputProcessor {

    /**
     * Set this for default debug localization reloading behaviour.
     */
    var reloadableLocalizationInstances: List<ILocalization>

}

class ToggleableDebugKeysInputProcessor(
    private val impl: IDebugKeysInputProcessor = DebugKeysInputProcessor(),
) : ToggleableInputProcessor(impl), IDebugKeysInputProcessor {

    override var reloadableLocalizationInstances: List<ILocalization> by impl::reloadableLocalizationInstances

}

class DebugKeysInputProcessor() : InputAdapter(), IDebugKeysInputProcessor {

    override var reloadableLocalizationInstances: List<ILocalization> = emptyList()

    private var shouldToggleDebugAfterKeyUp: Boolean = true

    override fun keyDown(keycode: Int): Boolean {
        if (Gdx.input.isKeyPressed(Paintbox.DEBUG_KEY)) {
            var pressed = true
            when (keycode) {
                Input.Keys.I -> {
                    val localizationInstances = reloadableLocalizationInstances.takeIf { it.isNotEmpty() }
                    if (localizationInstances != null) {
                        val nano = measureNanoTime {
                            localizationInstances.forEach { loc ->
                                loc.reloadAll()
                                loc.logMissingLocalizations(true)
                            }
                        }
                        Paintbox.LOGGER.debug(
                            "Reloaded I18N (${localizationInstances.size} instance(s)) from files in ${nano / 1_000_000.0} ms",
                            tag = "I18N"
                        )

                        val uniqueKeys: Set<String> =
                            localizationInstances.flatMap { it.getAllUniqueKeysForAllLocales() }.toSet()
                        Paintbox.LOGGER.debug(
                            "Total of ${uniqueKeys.size} unique keys across all localization instances",
                            tag = "I18N"
                        )
                    } else {
                        Paintbox.LOGGER.debug(
                            "No I18N to reload, DebugKeysInputProcessor#${::reloadableLocalizationInstances.name} was empty",
                            tag = "I18N"
                        )
                    }
                }

                Input.Keys.S -> {
                    val old = Paintbox.uiDebugOutlines.getOrCompute()
                    val isShiftDown = Gdx.input.isShiftDown()
                    Paintbox.uiDebugOutlines.set(
                        when (old) {
                            NONE -> if (isShiftDown) ALL else ONLY_VISIBLE
                            ALL -> if (isShiftDown) ONLY_VISIBLE else NONE
                            ONLY_VISIBLE -> if (isShiftDown) ALL else NONE
                        }
                    )
                    Paintbox.LOGGER.debug("Toggled UI debug outlines to ${Paintbox.uiDebugOutlines}")
                }

                Input.Keys.G -> System.gc()
                else -> {
                    pressed = false
                }
            }
            if (shouldToggleDebugAfterKeyUp && pressed) {
                shouldToggleDebugAfterKeyUp = false
            }
            if (pressed) {
                return true
            }
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        if (keycode == Paintbox.DEBUG_KEY) {
            val shouldToggle = shouldToggleDebugAfterKeyUp
            shouldToggleDebugAfterKeyUp = true
            if (shouldToggle) {
                val debugModeVar = Paintbox.debugMode

                val old = debugModeVar.get()
                debugModeVar.set(!old)

                Paintbox.LOGGER.debug("Switched debug mode to ${!old}")
                return true
            }
        }
        return false
    }
}