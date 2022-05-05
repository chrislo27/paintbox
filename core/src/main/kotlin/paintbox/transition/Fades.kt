package paintbox.transition


import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Interpolation
import paintbox.util.gdxutils.fillRect


abstract class SolidColorFade(
    duration: Float, color: Color, val direction: Direction, val interpolation: Interpolation = Interpolation.linear
) : Transition(duration) {

    enum class Direction {
        BECOME_OPAQUE, BECOME_TRANSPARENT
    }

    val color: Color = color.cpy()
    
    override fun render(transitionScreen: TransitionScreen, screenRender: () -> Unit) {
        screenRender()

        val camera = transitionScreen.main.nativeCamera
        val batch = transitionScreen.main.batch
        transitionScreen.main.resetViewportToScreen()
        batch.projectionMatrix = camera.combined
        batch.begin()
        var alphaMultiplier = interpolation.apply(0f, 1f, transitionScreen.percentageCurrent.coerceIn(0f, 1f))
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


class FadeToOpaque(duration: Float, color: Color, interpolation: Interpolation = Interpolation.linear)
    : SolidColorFade(duration, color, Direction.BECOME_OPAQUE, interpolation)
class FadeToTransparent(duration: Float, color: Color, interpolation: Interpolation = Interpolation.linear)
    : SolidColorFade(duration, color, Direction.BECOME_TRANSPARENT, interpolation)


@Deprecated("Deprecated in favour of FadeToOpaque", replaceWith = ReplaceWith("FadeToOpaque"))
typealias FadeOut = FadeToOpaque
@Deprecated("Deprecated in favour of FadeToTransparent", replaceWith = ReplaceWith("FadeToTransparent"))
typealias FadeIn = FadeToTransparent
