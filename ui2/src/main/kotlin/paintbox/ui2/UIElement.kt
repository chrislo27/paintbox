package paintbox.ui2

import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var


open class UIElement : UIBounds {

    //region UIBounds impl
    final override val bounds: Bounds = BoundsImpl()
    final override val margin: Var<Insets> = Var(Insets.ZERO)
    final override val border: Var<Insets> = Var(Insets.ZERO)
    final override val padding: Var<Insets> = Var(Insets.ZERO)
    final override val contentOffsetX: FloatVar = FloatVar(0f)
    final override val contentOffsetY: FloatVar = FloatVar(0f)
    final override val marginZone: ReadOnlyBounds get() = super.marginZone
    final override val borderZone: ReadOnlyBounds = UIBounds.createBorderZone(marginZone, margin)
    final override val paddingZone: ReadOnlyBounds = UIBounds.createPaddingZone(marginZone, margin, border)
    final override val contentZone: ReadOnlyBounds = UIBounds.createContentZone(marginZone, margin, border, padding, contentOffsetX, contentOffsetY)
    //endregion

    private val _parent: Var<UIElement?> = Var(null)
    val parent: ReadOnlyVar<UIElement?> get() = _parent
    
    private val _children: Var<List<UIElement>> = Var(emptyList())
    val children: ReadOnlyVar<List<UIElement>> get() = _children

    /**
     * The [SceneRoot] for this element, inferred by the [parent]'s [sceneRoot].
     * 
     * NB: [SceneRoot] sets this value to itself (i.e. the [sceneRoot] of a [SceneRoot] is itself).
     */
    val sceneRoot: ReadOnlyVar<SceneRoot?> = Var { // This must be backed by a Var
        parent.use()?.sceneRoot?.use()
    }
    
    //region Children operations

    /**
     * To only be used by the parent of this [UIElement] to set/unset the [parent] property.
     */
    open fun setParent(parent: UIElement?) {
        _parent.set(parent)
    }
    
    /**
     * Adds the [child] at the given [atIndex] of this [UIElement]'s children list.
     * Returns true if the element was added.
     */
    fun addChild(atIndex: Int, child: UIElement): Boolean {
        val currentChildren = children.getOrCompute()
        if (child !in currentChildren) {
            child.parent.getOrCompute()?.removeChild(child)

            val childrenCopy = ArrayList<UIElement>(currentChildren.size + 1)
            childrenCopy.addAll(currentChildren)
            childrenCopy.add(atIndex, child)
            _children.set(childrenCopy)

            child.setParent(this)
            this.onChildAdded(child, atIndex)
            child.onAddedToParent(this)

            return true
        }
        return false
    }

    /**
     * Adds the [child] to the end of this [UIElement]'s children list.
     */
    fun addChild(child: UIElement): Boolean {
        return addChild(children.getOrCompute().size, child)
    }

    /**
     * Adds the [child] to the beginning of this [UIElement]'s children list.
     */
    fun addChildToBeginning(child: UIElement): Boolean {
        return addChild(0, child)
    }

    /**
     * Removes the given [child] from this [UIElement].
     */
    fun removeChild(child: UIElement): Boolean {
        return removeChild(children.getOrCompute().indexOf(child))
    }

    /**
     * Removes the child at the given [index] from this [UIElement].
     * If the [index] is not in bounds, no action is taken.
     * Returns true if the child at the index was removed.
     */
    fun removeChild(index: Int): Boolean {
        val currentChildren = children.getOrCompute()
        if (index !in currentChildren.indices) return false

        val child = currentChildren[index]
//        if (child is Focusable) { // TODO re-add when Focusable is re-implemented
//            // Remove focus on this child.
//            val childSceneRoot = child.sceneRoot.getOrCompute()
//            childSceneRoot?.setFocusedElement(null)
//        }

        _children.set(currentChildren.toMutableList().apply {
            removeAt(index)
        })
        child.setParent(null)
        this.onChildRemoved(child, index)
        child.onRemovedFromParent(this)

        return true
    }

    fun removeAllChildren(): Int {
        val childrenSize = children.getOrCompute().size
        if (childrenSize > 0) {
            // Delete backwards
            for (i in childrenSize - 1 downTo 0) {
                removeChild(i)
            }
        }
        return childrenSize
    }

    operator fun plusAssign(child: UIElement) {
        addChild(child)
    }

    operator fun minusAssign(child: UIElement) {
        removeChild(child)
    }

    /**
     * Called when a child is added to this [UIElement]. This will be called BEFORE the call to [onAddedToParent].
     */
    protected open fun onChildAdded(newChild: UIElement, atIndex: Int) {
    }

    /**
     * Called when a child is removed from this [UIElement]. This will be called BEFORE the call to [onRemovedFromParent].
     */
    protected open fun onChildRemoved(oldChild: UIElement, oldIndex: Int) {
    }

    /**
     * Called when this [UIElement] is added to a parent. This will be called AFTER the call to [onChildAdded].
     */
    protected open fun onAddedToParent(newParent: UIElement) {
    }

    /**
     * Called when this [UIElement] is removed from a parent. This will be called AFTER the call to [onChildRemoved].
     */
    protected open fun onRemovedFromParent(oldParent: UIElement) {
    }
    
    //endregion

    operator fun contains(other: UIElement): Boolean {
        return other in children.getOrCompute()
    }
    
    //region Min/pref/max width/height defaults
    open fun prefWidth(height: Float): Float = this.bounds.width.get().valueOrZero()
    open fun minWidth(height: Float): Float = prefWidth(height)
    open fun maxWidth(height: Float): Float = prefWidth(height)
    
    open fun prefHeight(width: Float): Float = this.bounds.height.get().valueOrZero()
    open fun minHeight(width: Float): Float = prefHeight(width)
    open fun maxHeight(width: Float): Float = prefHeight(width)
    //endregion

    /**
     * Returns zero if this float is negative or NaN, otherwise returns its value.
     */
    protected fun Float.valueOrZero() = if (this.isNaN() || this < 0f) 0f else this

    override fun toString(): String {
        return "{${this.javaClass.name}, x=${this.bounds.x.get()}, y=${this.bounds.y.get()}, w=${this.bounds.width.get()}, h=${this.bounds.height.get()}}"
    }
}
