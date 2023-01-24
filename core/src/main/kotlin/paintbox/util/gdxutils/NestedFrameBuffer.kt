package paintbox.util.gdxutils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Pixmap
import com.badlogic.gdx.graphics.glutils.FrameBuffer
import paintbox.util.IntRect
import paintbox.util.MutIntRect
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer


/**
 * Kotlin implementation of https://github.com/crykn/guacamole/blob/master/gdx/src/main/java/de/damios/guacamole/gdx/graphics/NestableFrameBuffer.java.
 *
 * Not using guacamole to avoid dependencies.
 */
class NestedFrameBuffer : FrameBuffer {

    companion object {

        private val INT_BUFFER: IntBuffer =
            ByteBuffer.allocateDirect(16 * Int.SIZE_BYTES).order(ByteOrder.nativeOrder()).asIntBuffer()

        /**
         * Returns the currently bound framebuffer handle.
         */
        @Synchronized
        fun getBoundFBOHandle(): Int {
            val buf = INT_BUFFER
            Gdx.gl.glGetIntegerv(GL20.GL_FRAMEBUFFER_BINDING, buf)
            return buf.get(0)
        }

        /**
         * Stores x, y, width, height coordinates of the viewport into [rect].
         */
        @Synchronized
        fun getViewport(rect: MutIntRect) {
            val buf = INT_BUFFER
            Gdx.gl.glGetIntegerv(GL20.GL_VIEWPORT, buf)

            rect.x = buf.get(0)
            rect.y = buf.get(1)
            rect.width = buf.get(2)
            rect.height = buf.get(3)
        }
    }

    private var prevFBOHandle: Int = -1
    private var prevViewport: MutIntRect = MutIntRect(0, 0, 0, 0)

    constructor(format: Pixmap.Format, width: Int, height: Int, hasDepth: Boolean)
            : super(format, width, height, hasDepth)

    constructor(format: Pixmap.Format, width: Int, height: Int, hasDepth: Boolean, hasStencil: Boolean)
            : super(format, width, height, hasDepth, hasStencil)

    override fun build() {
        val prevFBO = getBoundFBOHandle()
        super.build()
        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, prevFBO)
    }

    override fun begin() {
        prevFBOHandle = getBoundFBOHandle()
        bind()
        getViewport(prevViewport)
        setFrameBufferViewport()
    }

    override fun end() {
        this.end(prevViewport.x, prevViewport.y, prevViewport.width, prevViewport.height)
    }

    override fun end(x: Int, y: Int, width: Int, height: Int) {
        val currentBoundHandle = getBoundFBOHandle()
        if (currentBoundHandle != this.framebufferHandle) {
            error("Currently bound framebuffer ($currentBoundHandle) doesn't match. Check begin/end order.")
        }

        Gdx.gl20.glBindFramebuffer(GL20.GL_FRAMEBUFFER, prevFBOHandle)
        Gdx.gl20.glViewport(x, y, width, height)
    }
}