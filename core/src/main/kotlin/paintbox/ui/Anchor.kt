package paintbox.ui

import paintbox.binding.Var

/**
 * The anchor system provides utilities for setting/binding the dimensional [bounds][UIElement.bounds]
 * properties. Nested sub-classes inside [Anchor] implement various anchor types, like centering and right alignment.
 *
 * The [configure] functions let you configure both the x and y dimensions at the same time.
 * If only one dimension at a time has to be edited, then the [xConfigure] and [yConfigure] functions can be used.
 */
sealed class Anchor {

    /**
     * The offsets are relative to the top left corner of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object TopLeft : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            element.bounds.x.set(offsetX)
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            element.bounds.y.set(offsetY)
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            element.bounds.x.bind {
                offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            element.bounds.y.bind {
                offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the midpoint of the left edge of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object CentreLeft : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            element.bounds.x.set(offsetX)
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            element.bounds.x.bind {
                offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the bottom left corner of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object BottomLeft : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            element.bounds.x.set(offsetX)
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) - (element.bounds.height.use()) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            element.bounds.x.bind {
                offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) - (element.bounds.height.use()) + offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the top right corner of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object TopRight : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            element.bounds.y.set(offsetY)
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            element.bounds.y.bind {
                offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the midpoint of the right edge of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object CentreRight : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the bottom right corner of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object BottomRight : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) - (element.bounds.height.use()) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) - (element.bounds.width.use()) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) - (element.bounds.height.use()) + offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the midpoint of the top edge of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object TopCentre : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            element.bounds.y.set(offsetY)
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use()
                    ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            element.bounds.y.bind {
                offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the midpoint of the bottom edge of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object BottomCentre : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) - (element.bounds.height.use()) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use()
                    ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) - (element.bounds.height.use()) + offsetY.invoke(this)
            }
        }
    }

    /**
     * The offsets are relative to the centre-point of the anchor parent.
     * Positive X offset is further RIGHT, positive Y offset is further DOWN.
     */
    object Centre : Anchor() {

        override fun xConfigure(element: UIElement, offsetX: Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use() ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use() ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY
            }
        }

        override fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.x.bind {
                (parent.use()?.contentZone?.width?.use()
                    ?: 0f) / 2f - (element.bounds.width.use() / 2f) + offsetX.invoke(this)
            }
        }

        override fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float) {
            val parent = element.parent
            element.bounds.y.bind {
                (parent.use()?.contentZone?.height?.use()
                    ?: 0f) / 2f - (element.bounds.height.use() / 2f) + offsetY.invoke(this)
            }
        }
    }

    /**
     * Configures the [element] based on this [Anchor], only on the X axis.
     */
    abstract fun xConfigure(element: UIElement, offsetX: Float)

    /**
     * Configures the [element] based on this [Anchor] with bindable offsets to the bounds x [Var] context.
     *
     * The context in the [offsetX] function parameters is the [Var.Context] of the bounds x [Var].
     */
    abstract fun xConfigure(element: UIElement, offsetX: Var.Context.() -> Float)

    /**
     * Configures the [element] based on this [Anchor], only on the X axis.
     */
    abstract fun yConfigure(element: UIElement, offsetY: Float)

    /**
     * Configures the [element] based on this [Anchor] with bindable offsets to the bounds y [Var] context.
     *
     * The context in the [offsetY] function parameters is the [Var.Context] of the bounds y [Var].
     */
    abstract fun yConfigure(element: UIElement, offsetY: Var.Context.() -> Float)


    /**
     * Configures the [element] based on this [Anchor] with constant offsets.
     */
    @Suppress("NOTHING_TO_INLINE", "RedundantModalityModifier")
    final inline fun configure(element: UIElement, offsetX: Float = 0f, offsetY: Float = 0f) {
        xConfigure(element, offsetX)
        yConfigure(element, offsetY)
    }

    /**
     * Configures the [element] based on this [Anchor] with bindable offsets to the bounds x/y [Var] context.
     *
     * The context in the [offsetX]/[offsetY] function parameters is the [Var.Context] of the bounds x/y [Var].
     */
    @Suppress("NOTHING_TO_INLINE", "RedundantModalityModifier")
    final inline fun configure(
        element: UIElement,
        noinline offsetX: Var.Context.() -> Float = { 0f },
        noinline offsetY: Var.Context.() -> Float = { 0f },
    ) {
        xConfigure(element, offsetX)
        yConfigure(element, offsetY)
    }

    /**
     * Configures the [element] based on this [Anchor] with constant offsets of 0.
     */
    @Suppress("NOTHING_TO_INLINE", "RedundantModalityModifier")
    final inline fun configure(element: UIElement) = configure(element, 0f, 0f)

}