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

    private val _parent: Var<Parent?> = Var(null)
    val parent: ReadOnlyVar<Parent?> get() = _parent

    /**
     * The [SceneRoot] for this element, inferred by the [parent]'s [sceneRoot].
     * 
     * NB: [SceneRoot] sets this value to itself (i.e. the [sceneRoot] of a [SceneRoot] is itself).
     */
    val sceneRoot: ReadOnlyVar<SceneRoot?> = Var { // This must be backed by a (Mutable)Var
        (parent.use() as? UIElement)?.sceneRoot?.use()
    }
    
    //region Children operations

    /**
     * To only be used by the new parent of this [UIElement] to set/unset the [parent] property.
     */
    open fun setParent(newParent: Parent?) {
        val oldParent = _parent.getOrCompute()
        if (oldParent != null) {
            onRemovedFromParent(oldParent)
        }
        
        _parent.set(newParent)
        
        if (newParent != null) {
            onAddedToParent(newParent)
        }
    }
    

    /**
     * Called when this [UIElement] is added to a parent.
     */
    protected open fun onAddedToParent(newParent: Parent) {
    }

    /**
     * Called when this [UIElement] is removed from a parent.
     */
    protected open fun onRemovedFromParent(oldParent: Parent) {
    }
    
    //endregion
    
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
