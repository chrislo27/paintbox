package paintbox.transition


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import paintbox.util.gdxutils.fillRect


abstract class SolidColorFade(
    duration: Float, color: Color, val direction: Direction,
    val interpolation: Interpolation = Interpolation.linear,
    val holdDuration: Float = 0f,
) : Transition(duration + holdDuration) {

    enum class Direction {
        BECOME_OPAQUE, BECOME_TRANSPARENT
    }

    val color: Color = color.cpy()
    val transitionDuration: Float = duration
    private val percentageAtHold: Float = if (this.duration <= 0f) 0f else when (direction) {
        Direction.BECOME_OPAQUE -> (transitionDuration / this.duration)
        Direction.BECOME_TRANSPARENT -> (holdDuration / this.duration)
    }

    override fun render(transitionScreen: TransitionScreen, screenRender: () -> Unit) {
        screenRender()

        val camera = transitionScreen.main.actualWindowSizeCamera
        val batch = transitionScreen.main.batch
        transitionScreen.main.resetViewportToScreen()
        batch.projectionMatrix = camera.combined
        batch.begin()

        val percentageCurrent = transitionScreen.percentageCurrent
        val modifiedPercentage = (if (holdDuration <= 0f) percentageCurrent else when (direction) {
            Direction.BECOME_OPAQUE -> (percentageCurrent / percentageAtHold).coerceIn(0f, 1f)
            Direction.BECOME_TRANSPARENT -> ((percentageCurrent - percentageAtHold) / (1f - percentageAtHold)).coerceIn(
                0f,
                1f
            )
        }).coerceIn(0f, 1f)
        var alphaMultiplier: Float = interpolation.apply(0f, 1f, modifiedPercentage.coerceIn(0f, 1f))
        if (direction == Direction.BECOME_TRANSPARENT) {
            alphaMultiplier = 1f - alphaMultiplier
        }

        batch.setColor(color.r, color.g, color.b, color.a * alphaMultiplier)
        batch.fillRect(0f, 0f, camera.viewportWidth * 1f, camera.viewportHeight * 1f)
        batch.setColor(1f, 1f, 1f, 1f)

        batch.end()
    }

    override fun dispose() {
    }
}


class FadeToOpaque(
    duration: Float,
    color: Color,
    interpolation: Interpolation = Interpolation.linear,
    holdDuration: Float = 0f,
) : SolidColorFade(duration, color, Direction.BECOME_OPAQUE, interpolation, holdDuration)

class FadeToTransparent(
    duration: Float,
    color: Color,
    interpolation: Interpolation = Interpolation.linear,
    holdDuration: Float = 0f,
) : SolidColorFade(duration, color, Direction.BECOME_TRANSPARENT, interpolation, holdDuration)


@Deprecated("Deprecated in favour of FadeToOpaque", replaceWith = ReplaceWith("FadeToOpaque"))
typealias FadeOut = FadeToOpaque
@Deprecated("Deprecated in favour of FadeToTransparent", replaceWith = ReplaceWith("FadeToTransparent"))
typealias FadeIn = FadeToTransparent
