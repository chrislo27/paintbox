package paintbox.ui2

import paintbox.binding.ReadOnlyVar
import paintbox.ui2.defaultimpl.DefaultUIBounds


@Suppress("RedundantModalityModifier")
final class SceneRoot(
    uiBounds: UIBounds = DefaultUIBounds(),
) : UIElement, UIBounds by uiBounds {

    override val parent: ReadOnlyVar<Parent?> = ReadOnlyVar.const(null)
    override val sceneRoot: ReadOnlyVar<SceneRoot?> = ReadOnlyVar.const(this)

    override fun Parent.setParent(newParent: Parent?) {
        // Intentionally NO-OP, a SceneRoot never has a parent
    }

    override fun toString(): String {
        return this.defaultToString()
    }
}