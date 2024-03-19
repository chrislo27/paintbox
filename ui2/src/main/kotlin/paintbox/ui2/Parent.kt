package paintbox.ui2

import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var


private val EMPTY_CHILDREN_VAR: ReadOnlyVar<List<UIElement>> = ReadOnlyVar.const(emptyList())

val UIElement.children: ReadOnlyVar<List<UIElement>>
    get() = (this as? Parent)?.children ?: EMPTY_CHILDREN_VAR


// For the future if Parent wants to restrict its children's types with generics 
private typealias Child = UIElement

interface Parent : UIElement {

    val children: Var<List<Child>>
    

    /**
     * Adds the [child] at the given [atIndex] of this children list.
     * Returns true if the element was added.
     */
    fun addChild(atIndex: Int, child: Child): Boolean {
        val currentChildren = children.getOrCompute()
        if (child !in currentChildren) {
            child.parent.getOrCompute()?.removeChild(child)

            val childrenCopy = ArrayList<Child>(currentChildren.size + 1)
            childrenCopy.addAll(currentChildren)
            childrenCopy.add(atIndex, child)
            children.set(childrenCopy)

            with(child) { setParent(this@Parent) }
            this.onChildAdded(child, atIndex)

            return true
        }
        return false
    }

    /**
     * Adds the [child] to the end of this children list.
     */
    fun addChild(child: Child): Boolean {
        return addChild(children.getOrCompute().size, child)
    }

    /**
     * Adds the [child] to the beginning of this children list.
     */
    fun addChildToBeginning(child: Child): Boolean {
        return addChild(0, child)
    }

    /**
     * Removes the given [child].
     */
    fun removeChild(child: Child): Boolean {
        return removeChild(children.getOrCompute().indexOf(child))
    }

    /**
     * Removes the child at the given [index].
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

        children.set(currentChildren.toMutableList().apply {
            removeAt(index)
        })
        with(child) { setParent(null) }
        this.onChildRemoved(child, index)

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

    operator fun plusAssign(child: Child) {
        addChild(child)
    }

    operator fun minusAssign(child: Child) {
        removeChild(child)
    }

    operator fun contains(other: Child): Boolean {
        return other in children.getOrCompute()
    }

    /**
     * Called when a child is added to this element. This will be called AFTER the call to [UIElement.onAddedToParent].
     */
    fun onChildAdded(newChild: Child, atIndex: Int) {
    }

    /**
     * Called when a child is removed from this element. This will be called AFTER the call to [UIElement.onRemovedFromParent].
     */
    fun onChildRemoved(oldChild: Child, oldIndex: Int) {
    }
    
}
