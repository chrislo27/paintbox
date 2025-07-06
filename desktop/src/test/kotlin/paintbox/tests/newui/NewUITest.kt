package paintbox.tests.newui

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.Screen
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Graphics
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import org.lwjgl.glfw.GLFW
import paintbox.PaintboxGame
import paintbox.PaintboxScreen
import paintbox.tests.TestPaintboxSettings
import paintbox.ui.*
import paintbox.util.gdxutils.fillRect
import paintbox.util.wave.WaveUtils


internal class NewUITestGame(paintboxSettings: TestPaintboxSettings, val screenFactory: (NewUITestGame) -> Screen) :
    PaintboxGame(paintboxSettings) {

    override fun getWindowTitle(): String {
        return "New UI test"
    }

    override fun create() {
        super.create()
        this.setScreen(screenFactory.invoke(this))
    }
}

internal class ScaledFontTestGame(paintboxSettings: TestPaintboxSettings) : PaintboxGame(paintboxSettings) {

    override fun getWindowTitle(): String {
        return "ScaledFont test"
    }

    override fun create() {
        super.create()

        val windowHandle = (Gdx.graphics as Lwjgl3Graphics).window.windowHandle
        GLFW.glfwSetWindowAspectRatio(windowHandle, 16, 9)
        this.setScreen(UIScaledFontTestScreen(this))
    }
}

internal class UIAnchorTestNestedScreen(override val main: NewUITestGame) : PaintboxScreen() {

    val camera = OrthographicCamera().apply {
        this.setToOrtho(false, Gdx.graphics.width.toFloat(), Gdx.graphics.height.toFloat())
        this.update()
    }
    val root: SceneRoot = SceneRoot(camera)
    val firstEle: UIElement = TestColorElement(Color.CHARTREUSE)
    val secondEle: UIElement = TestColorElement(Color.PURPLE)
    val thirdEle: UIElement = TestColorElement(Color.CYAN)

    init {
        root += firstEle.apply {
            this.bounds.x.set(100f)
            this.bounds.y.set(100f)
            this.bounds.width.set(500f)
            this.bounds.height.set(500f)

            addChild(secondEle.apply {
                this.bounds.x.set(50f)
                this.bounds.y.set(50f)
                this.bounds.width.set(300f)
                this.bounds.height.set(300f)

                addChild(thirdEle.apply {
                    this.bounds.x.set(25f)
                    this.bounds.y.set(25f)
                    this.bounds.width.set(100f)
                    this.bounds.height.set(70f)
                })
            })
        }
    }

    override fun render(delta: Float) {
        super.render(delta)

        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            firstEle.bounds.x.set(MathUtils.lerp(100f, 600f, WaveUtils.getSineWave(1.5f)))
            secondEle.bounds.x.set(MathUtils.lerp(50f, 250f, WaveUtils.getSineWave(1.5f)))
            secondEle.bounds.y.set(MathUtils.lerp(0f, 100f, WaveUtils.getSineWave(1.5f)))
            thirdEle.bounds.x.set(MathUtils.lerp(25f, 125f, WaveUtils.getSineWave(1.125f)))
            thirdEle.bounds.y.set(MathUtils.lerp(0f, 50f, WaveUtils.getSineWave(0.75f)))
        } else {
            firstEle.bounds.x.set(100f)
            secondEle.bounds.x.set(50f)
            secondEle.bounds.y.set(50f)
            thirdEle.bounds.x.set(25f)
            thirdEle.bounds.y.set(25f)
        }

        val batch = main.batch
        batch.projectionMatrix = main.actualWindowSizeCamera.combined
        batch.begin()

        root.renderAsRoot(batch)

        batch.end()
    }

    override fun resize(width: Int, height: Int) {
        super.resize(width, height)
        root.resize(width.toFloat(), height.toFloat())
    }

    override fun dispose() {
    }
}

internal class TestColorElement(val color: Color) : UIElement() {
    companion object {

        private val TMP_COLOR: Color = Color(1f, 1f, 1f, 1f)
    }

    private var isMouseDown = false
    private var isMouseHovering = false
    var doClickFlash = false

    init {
        addInputEventListener { evt ->
            if (!doClickFlash) return@addInputEventListener false
            if (evt is ClickPressed) {
                if (evt.button == Input.Buttons.LEFT) {
                    Gdx.app.postRunnable {
                        isMouseDown = true
                    }
//                    println((this as UIBounds).toString())
                }
                true
            } else if (evt is ClickReleased) {
                if (evt.button == Input.Buttons.LEFT) {
                    Gdx.app.postRunnable {
                        isMouseDown = false
                    }
                }
                true
            } else false
        }
        addInputEventListener { evt ->
            if (!doClickFlash) return@addInputEventListener false
            if (evt is MouseEntered) {
                isMouseHovering = true
                true
            } else if (evt is MouseExited) {
                isMouseHovering = false
                true
            } else false
        }
    }

    override fun renderSelf(originX: Float, originY: Float, batch: SpriteBatch) {
        val renderBounds = this.paddingZone
        val x = renderBounds.x.get() + originX
        val y = originY - renderBounds.y.get()
        val w = renderBounds.width.get()
        val h = renderBounds.height.get()
        val packed = batch.packedColor
        TMP_COLOR.set(color)
        if (isMouseDown) {
            TMP_COLOR.set(1f - color.r, 1f - color.g, 1f - color.b, color.a)
        } else if (isMouseHovering) {
            TMP_COLOR.set(color.r + 0.3f, color.g + 0.3f, color.b + 0.3f, color.a)
        }
        batch.color = TMP_COLOR
        batch.fillRect(x, y - h, w, h)
        batch.packedColor = packed
    }
}
    