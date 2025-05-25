package paintbox.ui.control

import paintbox.binding.ContextBinding
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.font.Markup
import paintbox.font.PaintboxFont
import paintbox.ui.StringConverter
import paintbox.ui.UIElement


open class MenuButton<T>(startingList: List<T>, text: String, font: PaintboxFont = UIElement.defaultFont) :
    Button(text, font), HasItemDropdown<T> {

    override val items: Var<List<T>> = Var(startingList)

    override val itemStringConverter: Var<StringConverter<T>> = Var(ComboBox.getDefaultStringConverter())
    override var onItemSelected: (T) -> Unit = {}

    override val contextMenuDefaultWidth: FloatVar = FloatVar { this@MenuButton.bounds.width.use() }
    override val contextMenuMarkup: Var<Markup?> = Var.bind { this@MenuButton.markup.use() }
    override val contextMenuFont: Var<PaintboxFont> = Var.bind { this@MenuButton.font.use() }
    override val contextMenuItemStrConverter: Var<StringConverter<T>> =
        Var.bind { this@MenuButton.itemStringConverter.use() }

    constructor(startingList: List<T>, binding: ContextBinding<String>, font: PaintboxFont = UIElement.defaultFont)
            : this(startingList, "", font) {
        @Suppress("LeakingThis")
        this.text.bind(binding)
    }

    constructor(startingList: List<T>, bindable: ReadOnlyVar<String>, font: PaintboxFont = UIElement.defaultFont)
            : this(startingList, "", font) {
        @Suppress("LeakingThis")
        this.text.bind { bindable.use() }
    }

    init {
        @Suppress("LeakingThis")
        HasItemDropdown.setDefaultActionToDeployDropdown(this)
    }
}