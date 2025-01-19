package paintbox.tests.textblocks

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import paintbox.PaintboxGame
import paintbox.PaintboxScreen
import paintbox.PaintboxSettings
import paintbox.font.TextAlign
import paintbox.font.TextBlock
import paintbox.font.TextRun
import paintbox.util.gdxutils.drawRect
import paintbox.util.gdxutils.fillRect


internal class TextBlockTestGame(paintboxSettings: PaintboxSettings) : PaintboxGame(paintboxSettings) {

    override fun getWindowTitle(): String {
        return "Text block test"
    }

    override fun create() {
        super.create()
        this.setScreen(TextBlockTestScreen(this))
    }
}

internal class TextBlockTestScreen(override val main: TextBlockTestGame) : PaintboxScreen() {

    var textBlock: TextBlock = generateTextBlock()

    fun generateTextBlock(): TextBlock {
        val defaultFonts = main.defaultFonts
        return TextBlock(
            listOf(
                TextRun(defaultFonts.debugFont, "Test "),
                TextRun(defaultFonts.debugFontItalic, "italicized "),
                TextRun(defaultFonts.debugFont, "blocky! one newline\n"),

                TextRun(defaultFonts.debugFont, "Firebrick line 2, 2 newlines\n\n", Color.FIREBRICK),

                TextRun(defaultFonts.debugFont, "Big run ", scaleX = 2f, scaleY = 2f),
                TextRun(defaultFonts.debugFontItalic, "italicized ", scaleX = 2f, scaleY = 2f),
                TextRun(defaultFonts.debugFont, "blocky? one newline\n", scaleX = 2f, scaleY = 2f),

                TextRun(defaultFonts.debugFont, "Back to normal "),
                TextRun(defaultFonts.debugFontItalic, "subscript ", scaleX = 0.58f, scaleY = 0.58f, offsetYEm = -0.333f),
                TextRun(defaultFonts.debugFont, "not anymore... 3 newlines\n\n\nStart is on same TextRun"),

                TextRun(defaultFonts.debugFontItalic, "superscript ", offsetYEm = 1.333f),
                TextRun(defaultFonts.debugFont, "and not. 1 newline\n"),

                TextRun(defaultFonts.debugFont, "Let's carry over the y offset "),
                TextRun(defaultFonts.debugFontItalic, "superscript ", offsetYEm = 1.333f, carryOverOffsetY = true),
                TextRun(defaultFonts.debugFont, "what happens now? 1 nl\n"),

                TextRun(defaultFonts.debugFontItalic, "And normal italicized font again. 1 nl\n"),

                TextRun(defaultFonts.debugFont, "The quick brown fox jumps over the lazy dog. 1 nl\n"),
                TextRun(defaultFonts.debugFont, "The quick brown fox jumps over the lazy dog. 2 nl\n\n"),
                TextRun(defaultFonts.debugFont, "     The quick brown fox jumps over the lazy dog. 1 nl\n"),
                TextRun(defaultFonts.debugFont, "The quick brown fox jumps over the lazy dog. 3 nl\n\n\n"),
                TextRun(defaultFonts.debugFont, "The quick brown fox jumps over the lazy dog. 1 nl\n"),
                TextRun(defaultFonts.debugFont, "The quick brown fox jumps over the lazy dog."),
            ),
            /*.map { if (it.color == Color.argb8888(Color.WHITE)) it.copy(color = Color.argb8888(Color.BLACK)) else it }*/
        )
    }

    private var textAlign = TextAlign.LEFT

    override fun render(delta: Float) {
        val batch = main.batch
        batch.begin()
        batch.setColor(0.1f, 0.1f, 0.1f, 1f)
        batch.fillRect(0f, 0f, Gdx.graphics.width + 0f, Gdx.graphics.height + 0f)

        batch.setColor(1f, 1f, 1f, 1f)
        val textBlock = textBlock
        val startX = 100f
        val startY = Gdx.graphics.height - 100f

        textBlock.drawCompressed(batch, startX, startY, textBlock.width, textAlign)

        batch.setColor(0f, 1f, 0f, 1f)
        batch.drawRect(startX, startY - textBlock.height, textBlock.width, textBlock.height, 1f)
        batch.setColor(1f, 1f, 0f, 1f)
        batch.drawRect(
            startX,
            startY - textBlock.height + textBlock.firstCapHeight + textBlock.lastDescent,
            textBlock.width,
            textBlock.height - textBlock.lastDescent,
            1f
        )


        batch.end()
        super.render(delta)
    }

    override fun keyTyped(character: Char): Boolean {
        when (character) {
            'r' -> {
                textBlock = generateTextBlock()
                return true
            }

            '1' -> {
                textAlign = TextAlign.LEFT
                return true
            }

            '2' -> {
                textAlign = TextAlign.CENTRE
                return true
            }

            '3' -> {
                textAlign = TextAlign.RIGHT
                return true
            }
        }
        return false
    }

    override fun dispose() {
    }
}