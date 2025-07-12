package paintbox.ui.control

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2
import paintbox.PaintboxGame
import paintbox.binding.FloatVar
import paintbox.binding.ReadOnlyFloatVar
import paintbox.binding.ReadOnlyVar
import paintbox.binding.Var
import paintbox.ui.ClickPressed
import paintbox.ui.MouseInputEvent
import paintbox.ui.Scrolled
import paintbox.ui.TouchDragged
import paintbox.ui.skin.DefaultSkins
import paintbox.ui.skin.Skin
import paintbox.ui.skin.SkinFactory
import paintbox.util.ColorStack
import paintbox.util.MathHelper
import paintbox.util.gdxutils.fillRoundedRect
import kotlin.math.sign


open class Slider : Control<Slider>() {

    companion object {

        const val SLIDER_SKIN_ID: String = "Slider"
        const val MIN_DEFAULT: Float = 0f
        const val MAX_DEFAULT: Float = 100f
        const val TICK_DEFAULT: Float = 10f

        init {
            DefaultSkins.register(SLIDER_SKIN_ID, SkinFactory { element: Slider ->
                SliderSkin(element)
            })
        }
    }

    val minimum: FloatVar = FloatVar(MIN_DEFAULT)
    val maximum: FloatVar = FloatVar(MAX_DEFAULT)
    val tickUnit: FloatVar = FloatVar(TICK_DEFAULT)
    private val _value: FloatVar = FloatVar(MIN_DEFAULT)
    val value: ReadOnlyFloatVar = _value


    init {
        minimum.addListener {
            setValue(_value.get())
        }
        maximum.addListener {
            setValue(_value.get())
        }

        val lastMouseRelativeToRoot = Vector2(0f, 0f)
        addInputEventListener { event ->
            if (event is MouseInputEvent && (event is TouchDragged || event is ClickPressed)) {
                if (pressedState.getOrCompute().pressed) {
                    val lastMouseInside: Vector2 = this.getPosRelativeToRoot(lastMouseRelativeToRoot)
                    lastMouseRelativeToRoot.x = event.x - lastMouseInside.x
                    lastMouseRelativeToRoot.y = event.y - lastMouseInside.y

                    val endCap = bounds.height.get() * 0.4f
                    setValue(
                        convertPercentageToValue(
                            ((lastMouseRelativeToRoot.x - endCap) / (bounds.width.get() - endCap * 2)).coerceIn(
                                0f,
                                1f
                            )
                        )
                    )

                    event !is TouchDragged // TouchDragged should not be consumed
                } else false
            } else if (event is Scrolled) {
                setValue(value.get() - event.amountY.sign.toInt() * tickUnit.get())
                true
            } else false
        }
    }

    fun setValue(value: Float) {
        val tick = tickUnit.get().coerceAtLeast(0f)
        val snapped = if (tick > 0f) {
            MathHelper.snapToNearest(value, tick)
        } else value
        _value.set(snapped.coerceIn(minimum.get(), maximum.get()))
    }

    protected open fun getArrowButtonTexReg(): TextureRegion {
        return PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet.upArrow
    }

    protected fun convertValueToPercentage(v: Float): Float {
        val min = minimum.get()
        val max = maximum.get()
        return ((v - min) / (max - min)).coerceIn(0f, 1f)
    }

    protected fun convertPercentageToValue(v: Float): Float {
        val min = minimum.get()
        val max = maximum.get()
        return (v * (max - min) + min).coerceIn(min, max)
    }

    override fun getDefaultSkinID(): String = SLIDER_SKIN_ID


    open class SliderSkin(element: Slider) : Skin<Slider>(element) {

        val bgColor: Var<Color> = Var(Color(0.94f, 0.94f, 0.94f, 1f))
        val filledColor: Var<Color> = Var(Color(0.24f, 0.74f, 0.94f, 1f))

        val disabledBgColor: Var<Color> = Var(Color(0.94f, 0.94f, 0.94f, 1f))
        val disabledFilledColor: Var<Color> = Var(Color(0.616f, 0.616f, 0.616f, 1f))

        val filledColorToUse: ReadOnlyVar<Color> = Var {
            if (element.apparentDisabledState.use()) disabledFilledColor.use() else filledColor.use()
        }
        val bgColorToUse: ReadOnlyVar<Color> = Var {
            if (element.apparentDisabledState.use()) disabledBgColor.use() else bgColor.use()
        }

        override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
            val contentBounds = element.contentZone
            val rectX = contentBounds.x.get() + originX
            val rectY = originY - contentBounds.y.get()
            val rectW = contentBounds.width.get()
            val rectH = contentBounds.height.get()
            val lastPackedColor = batch.packedColor
            val opacity = element.apparentOpacity.get()
            val tmpColor = ColorStack.getAndPush()

            val lineH = rectH * 0.4f
            val linePad = 4f
            val circleH = rectH

            tmpColor.set(bgColorToUse.getOrCompute())
            tmpColor.a *= opacity
            batch.color = tmpColor
            batch.fillRoundedRect(
                rectX + linePad,
                rectY - rectH * 0.5f - lineH * 0.5f,
                rectW - linePad * 2,
                lineH,
                lineH * 0.5f
            )
            tmpColor.set(filledColorToUse.getOrCompute())
            tmpColor.a *= opacity
            batch.color = tmpColor
            val valueAsPercent = element.convertValueToPercentage(element._value.get())
            batch.fillRoundedRect(
                rectX + linePad, rectY - rectH * 0.5f - lineH * 0.5f,
                MathUtils.lerp(
                    (circleH * 0.5f).coerceAtMost(rectW * 0.5f),
                    (rectW - circleH * 0.5f).coerceAtLeast(rectW * 0.5f),
                    valueAsPercent
                ), lineH, lineH * 0.5f
            )

            val filledCircleMul = 0.97f
            tmpColor.mul(filledCircleMul, filledCircleMul, filledCircleMul, 1f)
            batch.color = tmpColor
            batch.draw(
                PaintboxGame.gameInstance.staticAssets.paintboxSpritesheet.circleFilled,
                rectX + (valueAsPercent * (rectW - circleH)),
                rectY - rectH,
                circleH,
                circleH
            )

            batch.packedColor = lastPackedColor
            ColorStack.pop()
        }

        override fun renderSelfAfterChildren(originX: Float, originY: Float, batch: SpriteBatch) {
        }
    }
}

