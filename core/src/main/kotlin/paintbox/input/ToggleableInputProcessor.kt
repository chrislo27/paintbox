package paintbox.input

import com.badlogic.gdx.InputProcessor
import paintbox.binding.BooleanVar


open class ToggleableInputProcessor(val delegateInputProcessor: InputProcessor) : InputProcessor {
    
    val enabled: BooleanVar = BooleanVar(true)
    
    override fun keyDown(keycode: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.keyDown(keycode)
        } else false
    }

    override fun keyUp(keycode: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.keyUp(keycode)
        } else false
    }

    override fun keyTyped(character: Char): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.keyTyped(character)
        } else false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.touchDown(screenX, screenY, pointer, button)
        } else false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.touchUp(screenX, screenY, pointer, button)
        } else false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.touchDragged(screenX, screenY, pointer)
        } else false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.mouseMoved(screenX, screenY)
        } else false
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.scrolled(amountX, amountY)
        } else false
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return if (enabled.get()) {
            delegateInputProcessor.touchCancelled(screenX, screenY, pointer, button)
        } else false
    }
}
