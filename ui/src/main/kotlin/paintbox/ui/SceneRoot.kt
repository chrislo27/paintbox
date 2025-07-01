package paintbox.ui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Camera
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.viewport.Viewport
import paintbox.Paintbox
import paintbox.binding.*
import paintbox.ui.animation.AnimationHandler
import paintbox.ui.contextmenu.ContextMenu
import paintbox.util.RectangleStack
import paintbox.util.gdxutils.drawRect
import paintbox.util.viewport.NoOpViewport


/**
 * The [SceneRoot] element has the position 0, 0 and always has the width and height of the UI screen space.
 */
class SceneRoot(val viewport: Viewport) : UIElement() {

    companion object {

        val DEFAULT_DEBUG_OUTLINE_COLOR: Color = Color(0f, 1f, 0f, 1f)
    }

    data class MousePosition(val x: FloatVar, val y: FloatVar)

    val camera: Camera = viewport.camera

    /**
     * An optional camera that can be used if the scene root is being rendered to a framebuffer first and then
     * transformed later with another camera. This is used to make sure input vectors are transformed correctly.
     *
     * This assumes that [applyViewport] is false.
     */
    var postRenderCamera: Camera? = null

    private val tmpVec3: Vector3 = Vector3()
    private val mouseVector: Vector2 = Vector2()
    val mousePosition: MousePosition = MousePosition(FloatVar(0f), FloatVar(0f))

    val mainLayer: Layer = Layer("main", enableTooltips = true, exclusiveTooltipAccess = false, rootElement = this)
    val dialogLayer: Layer = Layer("dialog", enableTooltips = true, exclusiveTooltipAccess = true)
    val contextMenuLayer: Layer = Layer("contextMenu", enableTooltips = true, exclusiveTooltipAccess = true)
    val dropdownLayer: Layer = Layer("dropdown", enableTooltips = true, exclusiveTooltipAccess = true)
    val tooltipLayer: Layer = Layer("tooltip", enableTooltips = false, exclusiveTooltipAccess = false)
    val allLayers: List<Layer> = listOf(mainLayer, dialogLayer, contextMenuLayer, dropdownLayer, tooltipLayer)
    val allLayersReversed: List<Layer> = allLayers.asReversed()
    val contextMenuLikeLayers: List<Layer> = listOf(contextMenuLayer, dropdownLayer)

    val inputSystem: InputSystem = InputSystem(this)

    /**
     * A var that is always updated at the start of [renderAsRoot].
     */
    val frameUpdateTrigger: ReadOnlyBooleanVar = BooleanVar(false)
    val animations: AnimationHandler = AnimationHandler()

    /**
     * If true, applies the [viewport] during [renderAsRoot]. This should be false if this scene is being rendered
     * to a framebuffer.
     */
    val applyViewport: BooleanVar = BooleanVar(true)

    val currentElementWithTooltip: ReadOnlyVar<HasTooltip?> = Var(null)
    val currentTooltipVar: ReadOnlyVar<UIElement?> = Var(null)
    private var currentTooltip: UIElement? = null

    private var rootContextMenu: ContextMenu? = null
    private var dropdownContextMenu: ContextMenu? = null
    private var rootDialogElement: UIElement? = null

    private val _currentFocused: Var<Focusable?> = Var(null)
    val currentFocusedElement: ReadOnlyVar<Focusable?> = _currentFocused

    val debugOutlineColor: Var<Color> = Var(DEFAULT_DEBUG_OUTLINE_COLOR.cpy())

    constructor(camera: OrthographicCamera) : this(NoOpViewport(camera)) {
        applyViewport.set(false)
    }

    init {
        (sceneRoot as Var).set(this)
        this.doClipping.bind(applyViewport)

        val width = camera.viewportWidth
        val height = camera.viewportHeight
        updateAllLayerBounds(width, height)
        currentTooltipVar.addListener { v ->
            val layer = tooltipLayer
            val root = layer.root
            val currentElement = currentElementWithTooltip.getOrCompute()
            val oldValue = currentTooltip
            val newValue = v.getOrCompute()
            if (oldValue != null) {
                root.removeChild(oldValue)
                currentElement?.onTooltipEnded(oldValue)
            }
            if (newValue != null) {
                root.addChild(newValue)
                currentElement?.onTooltipStarted(newValue)
            }
            currentTooltip = newValue
        }

        contextMenuLayer.root.addInputEventListener { event ->
            var inputConsumed = false
            if (event is TouchDown) {
                if (rootContextMenu != null) {
                    hideRootContextMenu()
                    inputConsumed = true
                }
            }
            inputConsumed
        }
        dropdownLayer.root.addInputEventListener { event ->
            var inputConsumed = false
            if (event is TouchDown) {
                if (dropdownContextMenu != null) {
                    hideDropdownContextMenu()
                    inputConsumed = true
                }
            }
            inputConsumed
        }

        dialogLayer.root.addInputEventListener { _ ->
            rootDialogElement != null // Dialog layer eats all input when active
        }

        // Remove dropdowns if scrolled
        addInputEventListener { e ->
            if (e is Scrolled && dropdownContextMenu != null) {
                hideDropdownContextMenu()
            }
            false
        }

        (allLayers - mainLayer).forEach { l -> (l.root.sceneRoot as Var).set(this) }
    }

    fun renderAsRoot(batch: SpriteBatch) {
        if (applyViewport.get()) {
            viewport.apply()
        }

        (frameUpdateTrigger as BooleanVar).invert()
        updateMouseVector()
        updateTooltipPosition()
        checkCurrentElementWithTooltipIsStillVisible()

        animations.frameUpdate(Gdx.graphics.deltaTime)

        for (layer in allLayers) {
            val layerRoot = layer.root
            val layerBounds = layerRoot.bounds
            val originX = layerBounds.x.get()
            val originY = layerBounds.y.get() + layerBounds.height.get()

            val currentClipRect = RectangleStack.getAndPush()
                .set(layerBounds.x.get(), layerBounds.y.get(), layerBounds.width.get(), layerBounds.height.get())
            layerRoot.render(originX, originY, batch, currentClipRect, layerBounds.x.get(), layerBounds.y.get())
            RectangleStack.pop()
        }

        val drawOutlines = Paintbox.uiDebugOutlines.getOrCompute()
        if (drawOutlines != Paintbox.UIDebugOutlineMode.NONE) {
            val lastPackedColor = batch.packedColor
            batch.color = this.debugOutlineColor.getOrCompute()
            val useOutlines = drawOutlines == Paintbox.UIDebugOutlineMode.ONLY_VISIBLE
            val isDialogPresent = this.rootDialogElement != null
            for (layer in allLayers) {
                if (isDialogPresent && layer == mainLayer) continue
                val layerRoot = layer.root
                val layerBounds = layerRoot.bounds
                val originX = layerBounds.x.get()
                val originY = layerBounds.y.get() + layerBounds.height.get()
                layer.root.drawDebugRect(originX, originY, batch, useOutlines)
            }
            batch.packedColor = lastPackedColor
        }
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        // NO-OP
    }

    private fun UIElement.drawDebugRect(originX: Float, originY: Float, batch: SpriteBatch, onlyVisible: Boolean) {
        val thisBounds = this.bounds
        val x = originX + thisBounds.x.get()
        val y = originY - thisBounds.y.get()
        val w = thisBounds.width.get()
        val h = thisBounds.height.get()
        if (onlyVisible && !this.apparentVisibility.get()) return
        batch.drawRect(x, y - h, w, h, 1f)

        val childOffsetX = originX + this.contentZone.x.get()
        val childOffsetY = originY - this.contentZone.y.get()
        this.children.getOrCompute().forEach { child ->
            child.drawDebugRect(childOffsetX, childOffsetY, batch, onlyVisible)
        }
    }

    override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        super.renderSelfAfterChildren(originX, originY, batch)
    }

    private fun updateAllLayerBounds(width: Float, height: Float, posX: Float = 0f, posY: Float = 0f) {
        // Intentionally updating this.bounds before other layers.
        bounds.also { b ->
            b.x.set(posX)
            b.y.set(posY)
            b.width.set(width)
            b.height.set(height)
        }
        for (layer in allLayers) {
            if (layer === mainLayer) continue
            val root = layer.root
            val bounds = root.bounds
            bounds.x.set(posX)
            bounds.y.set(posY)
            bounds.width.set(width)
            bounds.height.set(height)
        }
    }

    fun resize(width: Float, height: Float, posX: Float = 0f, posY: Float = 0f) {
        hideRootContextMenu()
        updateAllLayerBounds(width, height, posX, posY)
    }

    fun resize() {
        viewport.update(Gdx.graphics.width, Gdx.graphics.height)
        val camera = this.camera
        val zoom = (camera as? OrthographicCamera)?.zoom ?: 1f
        resize(
            camera.viewportWidth, camera.viewportHeight,
            camera.position.x - (zoom * camera.viewportWidth / 2.0f),
            camera.position.y - (zoom * camera.viewportHeight / 2.0f)
        )
    }

    fun <E> setFocusedElement(element: E?) where E : UIElement, E : Focusable {
        val current = _currentFocused.getOrCompute()
        if (current === element) return

        if (current != null) {
            _currentFocused.set(null)
            current.onFocusLost()

            GlobalFocusableListeners.onFocusLost(this, current)
        }

        _currentFocused.set(element)
        if (element != null) {
            element.onFocusGained()
            GlobalFocusableListeners.onFocusGained(this, element)
        }
    }

    private fun updateTooltipPosition(tooltip: UIElement? = currentTooltip) {
        if (tooltip == null) return

        val bounds = tooltip.bounds
        val width = bounds.width.get()
        val height = bounds.height.get()
        val mouseX = mousePosition.x.get()
        val mouseY = mousePosition.y.get()
        val rootWidth = this.bounds.width.get()
        val rootHeight = this.bounds.height.get()
        val rightAlign = (mouseY <= height)
        bounds.y.set((mouseY - height).coerceAtMost(rootHeight - height).coerceAtLeast(0f))
        bounds.x.set((if (rightAlign) (mouseX - width) else mouseX).coerceAtMost(rootWidth - width).coerceAtLeast(0f))
    }
    
    private fun checkCurrentElementWithTooltipIsStillVisible() {
        val currentElementWithTooltip = (currentElementWithTooltip.getOrCompute() as? UIElement) ?: return
        
        if (currentTooltip != null && !currentElementWithTooltip.apparentVisibility.get()) {
            cancelTooltip()
        }
    }

    /**
     * For [InputSystem] to call when the mouse starts hovering over a [HasTooltip] [UIElement].
     */
    fun startTooltip(element: HasTooltip, tooltipVar: ReadOnlyVar<UIElement?>) {
        val currentElementWithTooltip = currentElementWithTooltip as Var
        cancelTooltip()
        currentElementWithTooltip.set(element)
        (currentTooltipVar as Var).bind(tooltipVar)
    }

    /**
     * For [InputSystem] to call when the mouse stops hovering over the element with the active tooltip.
     */
    fun cancelTooltip() {
        val currentElementWithTooltip = currentElementWithTooltip as Var
        (currentTooltipVar as Var).set(null)
        currentElementWithTooltip.set(null)
    }

    /**
     * Shows the [contextMenu] as the root menu. This will hide the existing context menu if any.
     */
    fun showRootContextMenu(contextMenu: ContextMenu, suggestOffsetX: Float = 0f, suggestOffsetY: Float = 0f) {
        hideRootContextMenu()
        addContextMenuToScene(contextMenu, contextMenuLayer, suggestOffsetX, suggestOffsetY)
        rootContextMenu = contextMenu
        contextMenuLayer.resetHoveredElementPath()
    }

    /**
     * Hides the root context menu if any.
     */
    fun hideRootContextMenu(): ContextMenu? {
        val currentRootMenu = rootContextMenu ?: return null
        hideDropdownContextMenu()
        removeContextMenuFromScene(currentRootMenu, contextMenuLayer)
        rootContextMenu = null
        contextMenuLayer.resetHoveredElementPath()
        return currentRootMenu
    }

    /**
     * Shows the [contextMenu] as a dropdown menu. This will hide the existing dropdown context menu if any.
     */
    fun showDropdownContextMenu(contextMenu: ContextMenu, suggestOffsetX: Float = 0f, suggestOffsetY: Float = 0f) {
        hideDropdownContextMenu()
        addContextMenuToScene(contextMenu, dropdownLayer, suggestOffsetX, suggestOffsetY)
        dropdownContextMenu = contextMenu
        dropdownLayer.resetHoveredElementPath()
    }

    /**
     * Hides the dropdown context menu if any.
     */
    fun hideDropdownContextMenu(): ContextMenu? {
        val currentRootMenu = dropdownContextMenu ?: return null
        removeContextMenuFromScene(currentRootMenu, dropdownLayer)
        dropdownContextMenu = null
        dropdownLayer.resetHoveredElementPath()
        return currentRootMenu
    }


    /**
     * Hides the given context menu, searching top to bottom through the allowed layers.
     */
    fun hideContextMenuUnknownLayer(contextMenu: ContextMenu): Boolean {
        if (contextMenu in dropdownLayer.root) {
            hideDropdownContextMenu()
            return true
        }
        if (contextMenu in contextMenuLayer.root) {
            hideRootContextMenu()
            return true
        }
        return false
    }

    /**
     * Shows the [dialog] as the root dialog element. This will hide the existing dialog if any.
     */
    fun showRootDialog(dialog: UIElement) {
        hideRootDialog()
        rootDialogElement = dialog
        dialogLayer.root.addChild(dialog)
        dialogLayer.resetHoveredElementPath()
        cancelTooltip()
    }

    /**
     * Hides the root dialog element if any.
     */
    fun hideRootDialog(): UIElement? {
        val currentRootDialog = rootDialogElement ?: return null
        dialogLayer.root.removeChild(currentRootDialog)
        rootDialogElement = null
        dialogLayer.resetHoveredElementPath()
        cancelTooltip()
        return currentRootDialog
    }

    /**
     * Adds the [contextMenu] to the scene. The [contextMenu] should be a "menu child" of another [ContextMenu],
     * but all context menus reside on the same level of the scene graph.
     *
     * This function is called from [ContextMenu.addChildMenu] so you should not call this on your own.
     *
     * To show a root context menu, call [showRootContextMenu].
     *
     * This does NOT connect the parent-child
     * relationship. One should call [ContextMenu.addChildMenu] for that.
     */
    fun addContextMenuToScene(
        contextMenu: ContextMenu,
        layer: Layer,
        suggestOffsetX: Float = 0f,
        suggestOffsetY: Float = 0f,
    ) {
        // Add to the provided layer scene
        // Compute the width/height layouts
        // Position the context menu according to its parent (if any)
        val root = layer.root
        if (contextMenu !in root.children.getOrCompute()) {
            root.addChild(contextMenu)

            contextMenu.computeSize(this)

            // Temporary impl: assumes they are only root context menus and positions it at the mouse
            val w = contextMenu.bounds.width.get()
            val h = contextMenu.bounds.height.get()
            var x = mousePosition.x.get() + suggestOffsetX
            var y = mousePosition.y.get() + suggestOffsetY

            val thisWidth = this.bounds.width.get()
            val thisHeight = this.bounds.height.get()
            if (x + w > thisWidth) x = thisWidth - w
            if (x < 0f) x = 0f
            if (y + h > thisHeight) y = thisHeight - h
            if (y < 0f) y = 0f

            contextMenu.bounds.x.set(x)
            contextMenu.bounds.y.set(y)

            // TODO position the context menu according to its parent if NOT the root

            contextMenu.onAddedToScene.invoke(this)

            cancelTooltip()
        }
    }

    /**
     * Removes the [contextMenu] from the scene. The [contextMenu] may be a "menu child" of another [ContextMenu],
     * but all context menus reside on the same level of the scene graph.
     * Any children of [contextMenu] will NOT be removed, that is the responsiblity of [ContextMenu.removeChildMenu].
     *
     * This function is called from [ContextMenu.removeChildMenu] so you should not call this on your own.
     *
     * To hide a root context menu, call [hideRootContextMenu].
     *
     * This does NOT disconnect the parent-child
     * relationship. One should call [ContextMenu.removeChildMenu] for that.
     */
    fun removeContextMenuFromScene(contextMenu: ContextMenu, layer: Layer) {
        // Remove from the provided layer scene
        val root = layer.root
        if (root.removeChild(contextMenu)) {
            contextMenu.onRemovedFromScene.invoke(this)
            cancelTooltip()
        }
    }

    fun isContextMenuActive(): Boolean = rootContextMenu != null
    fun getCurrentRootContextMenu(): UIElement? = rootContextMenu

    fun isDialogActive(): Boolean = rootDialogElement != null
    fun getCurrentRootDialog(): UIElement? = rootDialogElement

    private fun updateMouseVector() {
        val vector = mouseVector
        screenToUI(vector.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()))
        mousePosition.x.set(vector.x)
        mousePosition.y.set(vector.y)
    }

    /**
     * Converts screen coordinates (from gdx Input) to local UI coordinates.
     * This [SceneRoot]'s width and height are assumed to span the entire window
     * from Gdx.graphics.getWidth() and Gdx.graphics.getHeight(), with x and y offsets accordingly.
     * @return The mutated [vector]
     */
    fun screenToUI(vector: Vector2): Vector2 {
        tmpVec3.set(vector, 0f) // tmpVec3 is top-down, screen width/height

        val prCamera = this.postRenderCamera
        if (prCamera != null) {
            prCamera.unproject(
                tmpVec3, viewport.screenX.toFloat(), viewport.screenY.toFloat(),
                viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat()
            )
            vector.x = tmpVec3.x
            vector.y = prCamera.viewportHeight - tmpVec3.y
        } else {
            viewport.unproject(tmpVec3) // tmpVec3 is bottom-up, viewport width/height
            vector.x = tmpVec3.x
            vector.y = viewport.worldHeight - tmpVec3.y
        }

        return vector
    }

    /**
     * Converts local UI coordinates to screen coordinates (from gdx Input).
     * This [SceneRoot]'s width and height are assumed to span the entire window
     * from Gdx.graphics.getWidth() and Gdx.graphics.getHeight(), with x and y offsets accordingly.
     * @return The mutated [vector]
     */
    fun uiToScreen(vector: Vector2): Vector2 {
        tmpVec3.set(vector, 0f)

        val prCamera = this.postRenderCamera
        if (prCamera != null) {
            tmpVec3.y = prCamera.viewportHeight - tmpVec3.y
            prCamera.project(
                tmpVec3, viewport.screenX.toFloat(), viewport.screenY.toFloat(),
                viewport.screenWidth.toFloat(), viewport.screenHeight.toFloat()
            )
        } else {
            tmpVec3.y = viewport.worldHeight - tmpVec3.y
            viewport.project(tmpVec3)
        }

        vector.x = tmpVec3.x
        vector.y = Gdx.graphics.height - tmpVec3.y

        return vector
    }

    inner class Layer(
        val name: String, val enableTooltips: Boolean, val exclusiveTooltipAccess: Boolean,
        rootElement: UIElement = Pane(),
    ) {

        /**
         * Used by [InputSystem] for mouse-path tracking.
         */
        val lastHoveredElementPath: MutableList<UIElement> = mutableListOf()

        val root: UIElement = rootElement

        fun resetHoveredElementPath() {
            lastHoveredElementPath.clear()
            this@SceneRoot.cancelTooltip()
        }

        fun shouldEatTooltipAccess(): Boolean {
            return exclusiveTooltipAccess && lastHoveredElementPath.size >= 2
        }
    }

}