package paintbox.ui2

import paintbox.binding.ReadOnlyVar


interface UIElement : UIBounds {
    
    val parent: ReadOnlyVar<Parent?>

    /**
     * The [SceneRoot] for this element, inferred by the [parent]'s [sceneRoot].
     * 
     * NB: [SceneRoot] sets this value to itself (i.e. the [sceneRoot] of a [SceneRoot] is itself).
     */
    val sceneRoot: ReadOnlyVar<SceneRoot?>
    
    
    //region Children operations

    /**
     * To only be used by the new parent of this [UIElement] to set/unset the [parent] property.
     * 
     * Implementations will call [onRemovedFromParent] and [onAddedToParent].
     */
    fun setParent(newParent: Parent?) 
    

    /**
     * Called when this [UIElement] is added to a parent.
     */
    fun onAddedToParent(newParent: Parent) {
    }

    /**
     * Called when this [UIElement] is removed from a parent.
     */
    fun onRemovedFromParent(oldParent: Parent) {
    }
    
    //endregion
    
    //region Min/pref/max width/height defaults
    
    fun prefWidth(height: Float): Float = this.bounds.width.get().valueOrZero()
    fun minWidth(height: Float): Float = prefWidth(height)
    fun maxWidth(height: Float): Float = prefWidth(height)

    fun prefHeight(width: Float): Float = this.bounds.height.get().valueOrZero()
    fun minHeight(width: Float): Float = prefHeight(width)
    fun maxHeight(width: Float): Float = prefHeight(width)
    
    //endregion

    
    /**
     * Returns zero if this float is negative or NaN, otherwise returns its value.
     */
    fun Float.valueOrZero() = if (this.isNaN() || this < 0f) 0f else this
    
    fun UIElement.defaultToString(): String {
        return "{${this.javaClass.name}, x=${this.bounds.x.get()}, y=${this.bounds.y.get()}, w=${this.bounds.width.get()}, h=${this.bounds.height.get()}}"
    }
}
