package paintbox.ui

import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.math.Vector2
import paintbox.util.sumOfFloat


/**
 * Handles cascading the inputs from [InputProcessor] down to the [UIElement]s.
 *
 * If an event appears to not get propagated, check that it is not layered under its siblings, as the topmost element
 * will always receive inputs.
 */
class InputSystem(private val sceneRoot: SceneRoot) : InputProcessor {

    private enum class EventHandleResult {
        NOT_HANDLED,
        FILTERED_OUT,
        HANDLED,
    }

    private val vector: Vector2 = Vector2(0f, 0f)
    private val clickPressedList: MutableMap<Int, ClickPressedState> = mutableMapOf()
    private val pointerPressedButton: MutableMap<Int, Int> = mutableMapOf()

    /**
     * Represents the mouse x/y in UI space.
     */
    val mouseVector: Vector2
        get() = vector


    private fun propagateEventForLayer(layer: SceneRoot.Layer, evt: InputEvent): UIElement? {
        val lastPath = layer.lastHoveredElementPath
        var acceptedElement: UIElement? = null
        var anyFired = false

        // Filter
        for (i in 0..<lastPath.size) {
            val element = lastPath[i]
            val listeners = element.inputListeners.getOrCompute()
            if (listeners.any { l -> !l.filter(evt) }) {
                return null
            }
        }

        // Fire
        outer@ for (i in lastPath.size - 1 downTo 0) {
            anyFired = true
            val element = lastPath[i]
            val listeners = element.inputListeners.getOrCompute()
            for (l in listeners) {
                if (l.handle(evt)) {
                    // Consumed
                    acceptedElement = element
                    break@outer
                }
            }
        }

        if (anyFired) {
            val currentFocused = sceneRoot.currentFocusedElement.getOrCompute()
            if (evt is ClickPressed && currentFocused != null && sceneRoot.currentFocusedElement.getOrCompute() !== acceptedElement) {
                sceneRoot.setFocusedElement(null) // Unfocus if there's a ClickPressed event not on the currently focused element
            }
        }

        return acceptedElement
    }

    private fun dispatchEventBasedOnMouse(evt: InputEvent): Pair<SceneRoot.Layer, UIElement>? {
        for (layer in sceneRoot.allLayersReversed) {
            val result = propagateEventForLayer(layer, evt)
            if (result != null) {
                return layer to result
            }
        }
        return null
    }

    private fun dispatchFocusedEvent(evt: FocusedInputEvent): Boolean {
        val currentFocused = sceneRoot.currentFocusedElement.getOrCompute()
        if (currentFocused != null && currentFocused is UIElement /* instanceof should always be true but check is for smart-casting */) {
            return currentFocused.fireSingularEvent(evt) == EventHandleResult.HANDLED
        }
        return false
    }

    /**
     * Filters and fires the [event] based on this [UIElement]'s [UIElement.inputListeners].
     */
    private fun UIElement.fireSingularEvent(event: InputEvent): EventHandleResult {
        val listeners = this.inputListeners.getOrCompute()
        for (l in listeners) {
            if (!l.filter(event)) return EventHandleResult.FILTERED_OUT
            if (l.handle(event)) return EventHandleResult.HANDLED
        }
        return EventHandleResult.NOT_HANDLED
    }

    private fun updateDeepmostElementForMouseLocation(
        layer: SceneRoot.Layer,
        x: Float,
        y: Float,
        triggerTooltips: Boolean,
    ): Boolean {
        val lastPath: MutableList<UIElement> = layer.lastHoveredElementPath
        if (lastPath.isEmpty()) {
            val newPath = layer.root.pathToForInput(x, y)
            lastPath.add(layer.root)
            lastPath.addAll(newPath)
            var wasTooltipTriggered = false
            if (triggerTooltips) {
                wasTooltipTriggered = tooltipMouseEntered(newPath)
            }
            val mouseEnteredEvt = MouseEntered(x, y)
            newPath.forEach {
                it.fireSingularEvent(mouseEnteredEvt)
            }
            return wasTooltipTriggered
        }

        // Backtrack from last element to find the closest element containing the position, and then
        // find the deepest element starting from there.
        // Note that if the current last element is already the deepest element that contains x,y
        // then the rest of the code does nothing, achieving maximum performance.

        var cursor: UIElement? = lastPath.lastOrNull()
        var offX: Float = lastPath.sumOfFloat { it.contentZone.x.get() }
        var offY: Float = lastPath.sumOfFloat { it.contentZone.y.get() }
        if (cursor != null) {
            offX -= cursor.contentZone.x.get()
            offY -= cursor.contentZone.y.get()
        }
        // offsets should be the absolute x/y of the parent of cursor
        while (cursor != null && (
                    !cursor.borderZone.containsPointLocal(x - offX, y - offY)
                            || !cursor.apparentVisibility.get()
                    )
        ) {
            val removed = lastPath.removeLast()
            onMouseExited(removed)
            removed.fireSingularEvent(MouseExited(x, y))
            cursor = lastPath.lastOrNull()
            if (cursor != null) {
                offX -= cursor.contentZone.x.get()
                offY -= cursor.contentZone.y.get()
            }
        }

        /*
        Clipping check:
        - Follow the path back up the cursor
        - If at any point the cursor.doClipping == true, check:
          - Does the cursor X and Y fit within the bounds?
            - If not, trigger MouseExited for everything INCLUDING the cursor.
         */
        var clipCursor = cursor
        var clipOffX = offX
        var clipOffY = offY
        while (clipCursor != null) {
            if (clipCursor.doClipping.get() && !clipCursor.borderZone.containsPointLocal(x - clipOffX, y - clipOffY)) {
                // Delete and trigger mouse exited for everything up to and including clipCursor
                var lastRemoved: UIElement
                do {
                    val removed = lastPath.removeLast()
                    onMouseExited(removed)
                    removed.fireSingularEvent(MouseExited(x, y))
                    lastRemoved = removed
                    cursor = lastPath.lastOrNull()
                    if (cursor != null) {
                        offX -= cursor.contentZone.x.get()
                        offY -= cursor.contentZone.y.get()
                    }
                } while (lastRemoved !== clipCursor)
            }
            clipCursor = clipCursor.parent.getOrCompute()
            if (clipCursor != null) {
                clipOffX -= clipCursor.contentZone.x.get()
                clipOffY -= clipCursor.contentZone.y.get()
            }
        }


        // We found the closest parent that contains x, y, so we'll navigate to its deepest descendant that contains xy
        // starting from it, and the resulting "subpath" will be appended to our current path
        var wasTooltipTriggered = false
        if (cursor != null && cursor.children.getOrCompute().isNotEmpty()) {
            val subPath = cursor.pathToForInput(x - offX, y - offY)
            lastPath += subPath
            if (triggerTooltips) {
                wasTooltipTriggered = tooltipMouseEntered(subPath)
            }
            val mouseEnteredEvt = MouseEntered(x, y)
            subPath.forEach {
                it.fireSingularEvent(mouseEnteredEvt)
            }
        }

        return wasTooltipTriggered
    }

    private fun updateDeepmostElementForMouseLocation(x: Float, y: Float) {
        var wasTooltipTriggered = false
        for (layer in sceneRoot.allLayersReversed) {
            wasTooltipTriggered = updateDeepmostElementForMouseLocation(
                layer, x, y,
                layer.enableTooltips && !wasTooltipTriggered
            )
                    || wasTooltipTriggered || layer.shouldEatTooltipAccess()
        }
    }

    private fun tooltipMouseEntered(path: List<UIElement>): Boolean {
        for (element in path.asReversed()) {
            if (element is HasTooltip) {
                val tooltipElement = element.tooltipElement.getOrCompute()
                if (tooltipElement != null) {
                    sceneRoot.startTooltip(element, element.tooltipElement)
                    return true
                }
            }
        }
        return false
    }

    private fun onMouseExited(element: UIElement) {
        val currentTooltipElement = sceneRoot.currentElementWithTooltip.getOrCompute()
        if (currentTooltipElement != null && element === currentTooltipElement) {
            // The element the mouse was over should no longer show its tooltip
            sceneRoot.cancelTooltip()
        }
    }

    override fun keyDown(keycode: Int): Boolean {
        return dispatchFocusedEvent(KeyDown(keycode))
    }

    override fun keyUp(keycode: Int): Boolean {
        return dispatchFocusedEvent(KeyUp(keycode))
    }

    override fun keyTyped(character: Char): Boolean {
        return dispatchFocusedEvent(KeyTyped(character))
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val vec: Vector2 = sceneRoot.screenToUI(vector.set(screenX.toFloat(), screenY.toFloat()))
        updateDeepmostElementForMouseLocation(vec.x, vec.y)

        val touch = dispatchEventBasedOnMouse(TouchDown(vec.x, vec.y, button, pointer))
        val click = dispatchEventBasedOnMouse(ClickPressed(vec.x, vec.y, button))
        val allLayersPaths: Map<SceneRoot.Layer, List<UIElement>> =
            sceneRoot.allLayers.associateWith { it.lastHoveredElementPath.toList() }
        clickPressedList[button] = ClickPressedState(allLayersPaths, click)
        pointerPressedButton[pointer] = button

        return touch != null || click != null
    }

    private inline fun propagateEventFromClickPressedState(
        previousClick: ClickPressedState,
        eventFactory: (element: UIElement, layer: SceneRoot.Layer, lastHoveredElementPath: List<UIElement>) -> InputEvent,
    ): Boolean {
        var anyClick = false
        @Suppress("ReplaceManualRangeWithIndicesCalls")
        layerOuter@ for (layer in sceneRoot.allLayersReversed) {
            val lastHoveredElementPath = previousClick.lastHoveredElementPathPerLayer.getValue(layer)

            // Filter
            for (i in 0..<lastHoveredElementPath.size) {
                val element = lastHoveredElementPath[i]
                val listeners = element.inputListeners.getOrCompute()
                val evt = eventFactory(element, layer, lastHoveredElementPath)
                if (listeners.any { l -> !l.filter(evt) }) {
                    continue@layerOuter
                }
            }

            // Fire
            outer@ for (i in lastHoveredElementPath.size - 1 downTo 0) {
                val element = lastHoveredElementPath[i]
                val listeners = element.inputListeners.getOrCompute()
                val evt = eventFactory(element, layer, lastHoveredElementPath)
                for (l in listeners) {
                    if (l.handle(evt)) {
                        // Consumed
                        anyClick = true
                        break@outer
                    }
                }
            }

            if (anyClick) {
                break@layerOuter
            }
        }

        return anyClick
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        val vec: Vector2 = sceneRoot.screenToUI(vector.set(screenX.toFloat(), screenY.toFloat()))
        updateDeepmostElementForMouseLocation(vec.x, vec.y)

        val touch = dispatchEventBasedOnMouse(TouchUp(vec.x, vec.y, button, pointer))

        var anyClick = false
        val previousClick = clickPressedList[button]
        if (previousClick != null) {
            clickPressedList.remove(button)
            anyClick = propagateEventFromClickPressedState(previousClick) { element, layer, lastHoveredElementPath ->
                ClickReleased(
                    vec.x, vec.y, button,
                    element === previousClick.accepted?.second,
                    element in lastHoveredElementPath,
                    element in layer.lastHoveredElementPath && element.apparentVisibility.get()
                )
            }
        }
        pointerPressedButton.remove(pointer)

        return touch != null || anyClick
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        val vec: Vector2 = sceneRoot.screenToUI(vector.set(screenX.toFloat(), screenY.toFloat()))
        updateDeepmostElementForMouseLocation(vec.x, vec.y)

        var anyClick = false
        val pressedButton: Int? = pointerPressedButton[pointer]
        if (pressedButton != null) {
            val previousClick = clickPressedList[pressedButton]
            if (previousClick != null) {
                anyClick = propagateEventFromClickPressedState(previousClick) { element, layer, _ ->
                    TouchDragged(
                        vec.x, vec.y, pointer,
                        element in layer.lastHoveredElementPath && element.apparentVisibility.get()
                    )
                }
            }
        }

        return anyClick
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        val vec: Vector2 = sceneRoot.screenToUI(vector.set(screenX.toFloat(), screenY.toFloat()))
        updateDeepmostElementForMouseLocation(vec.x, vec.y)
        return dispatchEventBasedOnMouse(MouseMoved(vec.x, vec.y)) != null
    }

    override fun scrolled(amountX: Float, amountY: Float): Boolean {
        return dispatchEventBasedOnMouse(Scrolled(amountX, amountY)) != null
    }

    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        // Note: Only relevant on iOS and Android which Paintbox does not support.
        return touchUp(screenX, screenY, pointer, button)
    }

    private data class ClickPressedState(
        val lastHoveredElementPathPerLayer: Map<SceneRoot.Layer, List<UIElement>>,
        val accepted: Pair<SceneRoot.Layer, UIElement>?,
    )
}
