package paintbox.debug

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Matrix4
import com.badlogic.gdx.utils.Align
import paintbox.Paintbox
import paintbox.PaintboxGame
import paintbox.PaintboxScreen
import paintbox.util.MemoryUtils
import paintbox.util.gdxutils.drawCompressed


open class DebugOverlay(val paintboxGame: PaintboxGame) {

    protected val debugInfo: DebugInfo = paintboxGame.debugInfo
    val tmpMatrix: Matrix4 = Matrix4() // Used for rendering debug text

    open fun render() {
        val batch = paintboxGame.batch
        val paintboxFont = paintboxGame.debugFontBoldBordered
        val font = paintboxFont.begin()
        val fps = paintboxGame.getFPS()
        val memNumberFormat = debugInfo.memoryNumberFormat
        val msNumberFormat = debugInfo.msNumberFormat
        val screen = paintboxGame.screen
        val nativeCamera = paintboxGame.nativeCamera
        val string =
            """FPS: $fps (mspf min ${msNumberFormat.format(this.debugInfo.mspfMin)}, max ${msNumberFormat.format(this.debugInfo.mspfMax)}, avg ${
                msNumberFormat.format(
                    this.debugInfo.mspfAvg
                )
            })
Debug mode: ${Paintbox.DEBUG_KEY_NAME} + i: Reload I18N | + s (+Shift): UI outlines (${Paintbox.uiDebugOutlines}) | +g: gc
Version: ${paintboxGame.versionString} | Buffer: Logical ${Gdx.graphics.width}x${Gdx.graphics.height}, back buf.: ${Gdx.graphics.backBufferWidth}x${Gdx.graphics.backBufferHeight} (scale ${Gdx.graphics.backBufferScale})
Memory: ${memNumberFormat.format(Gdx.app.nativeHeap / 1024)} / ${memNumberFormat.format(MemoryUtils.maxMemoryKiB)} KiB (${
                memNumberFormat.format(
                    debugInfo.lastAllocationRateB / 1024
                )
            } KiB/s)
Screen: ${screen?.javaClass?.name}
${paintboxGame.getDebugString()}
${(screen as? PaintboxScreen)?.getDebugString() ?: ""}"""

        font.setColor(1f, 1f, 1f, 1f)
        font.drawCompressed(
            batch, string, 8f, nativeCamera.viewportHeight - 8f, nativeCamera.viewportWidth - 16f,
            Align.left
        )
        paintboxFont.end()
    }

}
