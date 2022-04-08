package paintbox.ui.control

import paintbox.binding.Var
import paintbox.ui.ActionablePane
import paintbox.ui.StringConverter
import paintbox.ui.contextmenu.ContextMenu
import paintbox.ui.contextmenu.MenuItem
import paintbox.ui.contextmenu.SeparatorMenuItem
import paintbox.ui.contextmenu.SimpleMenuItem
import kotlin.math.min


interface HasItemDropdown<T> {

    /**
     * Should be implemented by an item to show that it represents a separator in [setDefaultActionToDeployDropdown].
     */
    interface Separator

    val items: Var<List<T>> 
    @Suppress("UNCHECKED_CAST")
    val itemStringConverter: Var<StringConverter<T>>
    
    /**
     * Fired whenever an item was selected, even if it was already selected previously.
     */
    var onItemSelected: (T) -> Unit
    
    companion object {
        
        /**
         * Call to set the action to deploy a dropdown of choices. [onItemSelected] will be invoked when completed.
         */
        fun <AP, T> setDefaultActionToDeployDropdown(control: AP)
                where AP : ActionablePane, AP : HasItemDropdown<T>, AP : HasLabelComponent {
            _setDefaultActionToDeployDropdown(control) { item ->
                control.onItemSelected.invoke(item)
            }
        }

        /**
         * Call to set the action to deploy a dropdown of choices. [onItemSelected] will be invoked when completed.
         * The [control]'s [HasSelectedItem.selectedItem] will be set as well.
         */
        @JvmName("setDefaultActionToDeployDropdown_HasSelectedName")
        fun <AP, T> setDefaultActionToDeployDropdown(control: AP)
                where AP : ActionablePane, AP : HasItemDropdown<T>, AP : HasLabelComponent, AP : HasSelectedItem<T> {
            _setDefaultActionToDeployDropdown(control) { item ->
                control.selectedItem.set(item)
                control.onItemSelected.invoke(item)
            }
        }
        
        @Suppress("FunctionName")
        private fun <AP, T> _setDefaultActionToDeployDropdown(control: AP, menuItemAction: (item: T) -> Unit)
                where AP : ActionablePane, AP : HasItemDropdown<T>, AP : HasLabelComponent {
            /* 
            NOTE 2022-04-08 Kotlin 1.6.20:
            Compiler crash if this is not in a companion object and AP is the receiver. Workaround:
            put in a companion object and let receiver be an explicit parameter. 
            */
            with(control) {
                this.setOnAction {
                    val itemList: List<T> = items.getOrCompute()
                    val root = this.sceneRoot.getOrCompute()
                    if (itemList.isNotEmpty() && root != null) {
                        val ctxMenu = ContextMenu()
                        ctxMenu.defaultWidth.set(this.bounds.width.get())
                        val thisMarkup = this.markup.getOrCompute()
                        val thisFont = this.font.getOrCompute()
                        val strConverter = this.itemStringConverter.getOrCompute()
                        val menuItems: List<Pair<T, MenuItem>> = itemList.map { item: T ->
                            if (item is Separator) {
                                item to SeparatorMenuItem()
                            } else {
                                val scaleXY: Float = min(scaleX.get(), scaleY.get())
                                item to (if (thisMarkup != null)
                                    SimpleMenuItem.create(strConverter.convert(item), thisMarkup, scaleXY)
                                else SimpleMenuItem.create(strConverter.convert(item), thisFont, scaleXY)).also { smi ->
                                    smi.closeMenuAfterAction = true
                                    smi.onAction = {
                                        menuItemAction(item)
                                    }
                                }
                            }
                        }
                        menuItems.forEach {
                            ctxMenu.addMenuItem(it.second)
                        }

                        root.showDropdownContextMenu(ctxMenu)
                        if (ctxMenu.isContentInScrollPane && this is HasSelectedItem<*>) {
                            // Scroll the scroll pane down until we see the right one
                            val currentItem = this.selectedItem.getOrCompute()
                            val currentPair: Pair<T, MenuItem>? = menuItems.find { it.first === currentItem }
                            if (currentPair != null) {
                                ctxMenu.scrollToItem(currentPair.second)
                            }
                        }

                        // Reposition the context menu
                        val h = ctxMenu.bounds.height.get()
                        val thisRelativePos = this.getPosRelativeToRoot()
                        val thisY = thisRelativePos.y
                        ctxMenu.bounds.x.set(thisRelativePos.x)
                        val rootBounds = sceneRoot.getOrCompute()?.bounds
                        if (rootBounds != null) {
                            // Attempt to fit entire context menu below the combo box, otherwise put it above
                            val belowY = thisY + this.bounds.height.get()
                            if (belowY + h > rootBounds.height.get()) {
                                ctxMenu.bounds.y.set((thisY - h).coerceAtLeast(0f))
                            } else {
                                ctxMenu.bounds.y.set(belowY)
                            }

                            ctxMenu.bounds.x.set((thisRelativePos.x).coerceAtMost(rootBounds.width.get() - ctxMenu.bounds.width.get()))
                        } else {
                            ctxMenu.bounds.y.set(this.bounds.y.get() + this.bounds.height.get())
                        }
                    }
                }
            }
        }
    }
    
}

