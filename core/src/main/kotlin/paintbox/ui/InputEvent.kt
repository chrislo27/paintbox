package paintbox.ui


/**
 * Handles and optionally filters an [InputEvent].
 * 
 * Events are filtered "top-down" (from the parent to the child) and are handled "bottom-up" (child to parent).
 * 
 * By default, events are always filtered through and are not stopped.
 * 
 * **Special cases:** Not all events flow from the parent to the child. Some events only target a specific element,
 * such as [MouseEntered] and [MouseExited]. In these cases, the [filter] and [handle] functions act as expected, 
 * but with a chain of exactly one element.
 */
fun interface InputEventListener {

    /**
     * Called to handle the [event].
     * Returns true if the event has been consumed and event propagation should stop.
     */
    fun handle(event: InputEvent): Boolean

    /**
     * Called to filter the [event]. Return true to continue propagating the event, false to stop it and cancel it.
     * 
     * The default behaviour is to let the event though.
     */
    fun filter(event: InputEvent): Boolean {
        return true
    }
}

/**
 * An input event that is propagated to the [UIElement]s in a scene.
 */
open class InputEvent

/**
 * An [InputEvent] that has mouse x and y coordinates.
 */
open class MouseInputEvent(val x: Float, val y: Float) : InputEvent()

/**
 * An input event that will only be propagated if the [UIElement] is the current focus target.
 */
open class FocusedInputEvent : InputEvent()

class KeyDown(val keycode: Int) : FocusedInputEvent()
class KeyUp(val keycode: Int) : FocusedInputEvent()
class KeyTyped(val character: Char) : FocusedInputEvent()

class MouseMoved(x: Float, y: Float) : MouseInputEvent(x, y)
class Scrolled(val amountX: Float, val amountY: Float) : InputEvent()

/**
 * Represents the gdx touchDown event in InputProcessor.
 */
class TouchDown(x: Float, y: Float, val button: Int, val pointer: Int) : MouseInputEvent(x, y)

/**
 * Represents the gdx touchUp event in InputProcessor.
 */
class TouchUp(x: Float, y: Float, val button: Int, val pointer: Int) : MouseInputEvent(x, y)

/**
 * Represents the gdx touchDragged event in InputProcessor.
 */
class TouchDragged(x: Float, y: Float, val pointer: Int,
                   val isCurrentlyWithinBounds: Boolean) : MouseInputEvent(x, y)

/**
 * Fired when the mouse enters this UI element.
 */
class MouseEntered(x: Float, y: Float) : MouseInputEvent(x, y)

/**
 * Fired when the mouse exits this UI element.
 */
class MouseExited(x: Float, y: Float) : MouseInputEvent(x, y)

/**
 * Called when a mouse button is pressed on this element.
 */
class ClickPressed(x: Float, y: Float, val button: Int) : MouseInputEvent(x, y)

/**
 * Called when a mouse button is released on this element, having previously received the [ClickPressed]
 * event. [consumedPrior] will be true if this element previously consumed the [ClickPressed] event.
 */
class ClickReleased(x: Float, y: Float, val button: Int, val consumedPrior: Boolean,
                    val wasWithinBounds: Boolean, val isCurrentlyWithinBounds: Boolean) : MouseInputEvent(x, y)
