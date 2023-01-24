package paintbox.ui.control

import paintbox.binding.Var

interface HasSelectedItem<T> {

    val selectedItem: Var<T>
}
