package paintbox.font

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.utils.Align
import paintbox.binding.BooleanVar
import paintbox.binding.FloatVar
import paintbox.util.ColorStack
import paintbox.util.gdxutils.copy
import paintbox.util.gdxutils.scaleMul


/**
 * A list of [TextRun]s strung together as a block of text, with various metrics computed.
 * 
 * Also supports line wrapping during computation.
 */
class TextBlock(val runs: TextRunList) {

    /*
    TextBlock internal workings:
    TextBlock allows for combining multiple fonts and styles into a single text block.
    It also allows for render-time alignment changes (left, centre, right).
    
    In order to do this, TextBlock needs to be aware of each gdx GlyphRun and its position,
    as well as where each line is (for horizontal positioning).
    
    Each TextRun is mapped to a gdx GlyphLayout. Each GlyphLayout has a series of
    GlyphRuns, which has their own x/y positions.
    
    TextBlock will take all these "flattened" GlyphRuns and treat them as if they are strung together.
    It recomputes the x/y for all of them based on each TextRun's style. It will also be
    aware of the individual lines since that is used for horizontal alignment.
    
    For rendering purposes, each GlyphRun is then mapped back to its own GlyphLayout, which can be passed
    into the gdx BitmapFont draw function.
     */

    /**
     * A [TextRunInfo] has a [GlyphLayout], which internally has multiple glyph runs.
     * Each glyph run is mapped to a [GlyphRunInfo].
     */
    data class TextRunInfo(val run: TextRun, val currentFontNumber: Long, val glyphLayout: GlyphLayout) {

        val font: PaintboxFont = run.font
        val width: Float = glyphLayout.width
        val height: Float = glyphLayout.height
        val glyphRunInfo: List<GlyphRunInfo> = glyphLayout.runs.map { glyphRun ->
            // Note: as of libGDX 1.11, GlyphLayout runs are cached and shouldn't be used outside of GlyphLayout#setText
            GlyphRunInfo(this, glyphRun.copy())
        }
    }

    /**
     * Information about a [GlyphLayout.GlyphRun], such as its line number and position.
     * The parent [TextRunInfo] is accessible via [textRunInfo].
     */
    data class GlyphRunInfo(val textRunInfo: TextRunInfo, val glyphRun: GlyphLayout.GlyphRun) {

        private companion object {
            
            fun Int.argbToAbgr(): Int =
                (this and 0xFF00FF00u.toInt() /* A and G stay */) or ((this and 0xFF) shl 16 /* B */) or ((this and 0x00FF0000) ushr 16 /* R */)
        }

        val glyphRunAsLayout: GlyphLayout = GlyphLayout().also { l ->
            // NOTE: height is intentionally not computed since it isn't used for rendering.
            l.runs.clear()
            l.runs.setSize(1)
            l.runs[0] = glyphRun

            l.glyphCount = glyphRun.glyphs.size
            l.width = glyphRun.width

            l.colors.add(0) // Start glyph
            var argb = textRunInfo.run.color
            var alphaHex = ((argb ushr 24) and 0xFF)
            alphaHex = (alphaHex * textRunInfo.run.opacity).toInt().coerceIn(0, 0xFF)
            argb = (argb and 0x00FFFFFF) or (alphaHex shl 24)
            val runColorAbgr = argb.argbToAbgr()
            l.colors.add(runColorAbgr)
        }
        var lineIndex: Int = 0
        var posX: Float = 0f
        var posY: Float = 0f
    }

    data class LineInfo(val index: Int, val width: Float, val posY: Float)

    
    private val layoutInfoIsInvalid: BooleanVar = BooleanVar(true)
    private val tmpColorStack: MutableList<Color> = mutableListOf()

    //region Properties
    
    /**
     * The width where text lines longer than it are automatically wrapped.
     * If greater than zero, [computeLayouts] will apply line wrapping.
     */
    val lineWrapping: FloatVar = FloatVar(0f)

    //endregion
    
    //region Internally computed information
    
    /**
     * Internally computed text run information.
     */
    var runInfo: List<TextRunInfo> = listOf()
        private set
    
    /**
     * Internally computed text run information.
     */
    var lineInfo: List<LineInfo> = listOf()
        private set
    
    //endregion

    //region Metrics
    
    var width: Float = 0f
        private set
    var height: Float = 0f
        private set
    var firstCapHeight: Float = 0f
        private set
    var lastDescent: Float = 0f
        private set
    
    //endregion

    init {
        lineWrapping.addListener {
            invalidate()
        }
    }

    constructor(runs: TextRunList, wrapWidth: Float) : this(runs) {
        this.lineWrapping.set(wrapWidth)
    }


    private fun adjustFontForTextRun(font: BitmapFont, textRun: TextRun) {
        font.scaleMul(textRun.scaleX, textRun.scaleY)
        font.data.lineHeight *= textRun.lineHeightScale
        font.data.down = font.data.lineHeight * if (font.data.flipped) 1 else -1
    }

    private fun resetFontForTextRun(font: BitmapFont, textRun: TextRun) {
        font.data.lineHeight /= textRun.lineHeightScale
        font.data.down = font.data.lineHeight * if (font.data.flipped) 1 else -1
        font.scaleMul(1f / textRun.scaleX, 1f / textRun.scaleY)
    }

    private fun invalidate() {
        layoutInfoIsInvalid.set(true)
    }

    /**
     * May be used to prevent flickering in some UI elements -- the text bound info is usually only computed on first
     * render, but some of that info may be used as part of the render parameters.
     * 
     * @return True if layouts were recomputed
      */    
    fun computeLayoutsIfNeeded(): Boolean {
        if (isRunInfoInvalid()) {
            computeLayouts()
            return true
        }
        return false
    }

    fun computeLayouts() {
        var maxPosX = 0f
        var posX = 0f
        var posY = 0f
        this.firstCapHeight = 0f

        val lineInfo: MutableList<LineInfo> = mutableListOf()
        var currentLineWidth = 0f
        var currentLineIndex = 0
        var currentLineStartY = 0f

        val lineWrapWidth: Float = this.lineWrapping.get()
        val doLineWrapping: Boolean = lineWrapWidth > 0f

        val runInfo: List<TextRunInfo> = runs.map { textRun ->
            // Set font scales and metrics
            val paintboxFont = textRun.font
            val font = paintboxFont.begin()
            adjustFontForTextRun(font, textRun)

            val color = Color(1f, 1f, 1f, 1f)
            Color.argb8888ToColor(color, textRun.color)
            color.a *= textRun.opacity
            val textRunInfo = TextRunInfo(
                textRun, paintboxFont.currentFontNumber,
                if (doLineWrapping) {
                    val continuationLineWidth = (lineWrapWidth - currentLineWidth).coerceAtLeast(0f)
                    // Find the trailing line's wrap point since it may not start at x=0
                    var text = textRun.text
                    val gl = GlyphLayout()
                    // Don't wrap text here, we need to find the first line of runs
                    gl.setText(font, text, color, (lineWrapWidth).coerceAtLeast(0f), Align.left, false)
                    if (continuationLineWidth < lineWrapWidth && gl.runs.size > 0 && !textRun.text.startsWith("\n")) {
                        // The continuation line width is smaller, so find the wrap point there.
                        // But we need to verify that the line break is where it ought to be, and NOT
                        // a consequence of the (temp) smaller max width.   

                        // Only the first run matters for wrapping.    
                        val firstRunWidth = gl.runs.first().width
                        if (firstRunWidth > continuationLineWidth) {
                            // This contiguous block does NOT fit! Find a wrap point and inject a newline.

                            if (continuationLineWidth < font.data.spaceXadvance * 3) {
                                // Minimum wrap length in GlyphLayout. We should just put a newline immediately, otherwise we'll get very fragmented words
                                text =
                                    " \n" + text.trimStart() // The leading space is required to not trigger blankLineScale
                            } else {
                                gl.setText(font, textRun.text, color, continuationLineWidth, Align.left, true)
                                if (gl.runs.size >= 2) {
                                    // Inject the newline where the new run was added. The new run will always be
                                    // the second one, since the first original run will have been split.    
                                    val first = gl.runs[0]
                                    val wrapIndex = first.glyphs.size + 1
                                    if (wrapIndex in 1..<text.length) { // wrapIndex will always be >= 1
                                        val preChar = text[wrapIndex - 1]
                                        text = if (!font.data.isWhitespace(preChar)) {
                                            // If the char before the newline is not a breaking-allowed char, 
                                            // then we assume that this word was split in the middle. That looks bad,
                                            // so we'll split at the beginning of the run instead.
                                            // This case happens frequently if a new text run was added due to bolding etc.
                                            " \n" + text.trimStart() // The leading space is required to not trigger blankLineScale
                                        } else {
                                            text.substring(0, wrapIndex) + "\n" + text.substring(wrapIndex).trimStart()
                                        }
                                    }
                                }
                            }
                        }
                    }
                    gl.setText(font, text, color, (lineWrapWidth).coerceAtLeast(0f), Align.left, true)
                    gl
                } else {
                    GlyphLayout(font, textRun.text, color, 0f, Align.left, false)
                }
            )

            val offX = textRun.offsetXEm * font.spaceXadvance
            val offY = textRun.offsetYEm * font.xHeight
            val xAdvanceEm = textRun.xAdvanceEm * font.spaceXadvance
            val capHeight = font.data.capHeight
            if (this.firstCapHeight == 0f) {
                this.firstCapHeight = capHeight
            }

            // Each GlyphRun inside the GlyphLayout inside TextRunInfo is relative to 0, 0
            // Adjust x/y of GlyphRunInfo inside TextRunInfo
            val yBeforeGlyphRuns = posY

            // Offset by the TextRun offset
            posX += offX
            posY += offY

            if (currentLineStartY == 0f) {
                // First line needs to have its y position set
                currentLineStartY = posY
            }

            var lastGlyphRunRightEdge = 0f
            var updateCurrentLineStartY = false
            textRunInfo.glyphRunInfo.forEachIndexed { glyphRunInfoIndex, glyphRunInfo ->
                val glyphRun = glyphRunInfo.glyphRun

                glyphRunInfo.lineIndex = currentLineIndex

                if (glyphRunInfoIndex > 0) { // Fixes extra offset at very beginning of glyph run for first glyph
                    posX += glyphRun.glyphs.first().xoffset
                }

                // Update X/Y based on GlyphRun x/y
                if (glyphRun.x < lastGlyphRunRightEdge
                    || (glyphRunInfoIndex == 0 && textRun.text.isNotEmpty() && textRun.text[0] == '\n')
                ) {
                    // The glyph went back to the left. Consider this a new line
                    // It is also a new line if the text run begins with a newline (otherwise it would get appended to the last one)
                    currentLineWidth = posX
                    posX = 0f

                    lineInfo += LineInfo(currentLineIndex, currentLineWidth, currentLineStartY)
                    currentLineIndex++
                    currentLineWidth = 0f
                    updateCurrentLineStartY = true
                    glyphRunInfo.lineIndex = currentLineIndex
                } else {
                    posX += glyphRun.x
                    posY += glyphRun.y
                }

                glyphRunInfo.posX = posX
                // Adding capHeight makes the drawing position at the baseline
                glyphRunInfo.posY = posY + capHeight

                // Bump x forward by width of GlyphRun
//                posX += glyphRun.width
                // We have to account for the xAdvance of the last glyph.
                // GlyphLayout replaces it with the width of the last glyph instead. See GlyphLayout#adjustLastGlyph
                val glyphRunXadv = glyphRun.xAdvances
                var advanceWidth = 0f
                for (i in 0..<glyphRun.glyphs.size) {
                    advanceWidth += glyphRunXadv[i]
                }
                advanceWidth += glyphRun.glyphs.last().let { it.xadvance * font.data.scaleX }

                posX += advanceWidth
                if (posX > maxPosX) {
                    maxPosX = posX
                }

                lastGlyphRunRightEdge = glyphRun.x + glyphRun.width
            }

            // Reverse any x offsets if they are not to be carried over
            if (!textRun.carryOverOffsetX) {
                posX -= offX
            }

            // Move y down in case there are whitespace lines
            posX += xAdvanceEm
            posY = yBeforeGlyphRuns + -(textRunInfo.glyphLayout.height - capHeight)
            lastDescent = font.data.descent

            // New line check
            if (posY < yBeforeGlyphRuns) {
                // The posX should be where the last GlyphRun left off
                // However, if the run ends in newlines with optional whitespace, then posX should be 0
                val runText = textRunInfo.run.text
                val runEndsInNewlines = runText.trimEnd { it == ' ' }
                    .endsWith('\n') //textRunInfo.glyphLayout.height - capHeight > abs(lastGlyphRunY)
                if (runEndsInNewlines) {
                    currentLineWidth = posX
                    posX = 0f
                    // This is a new line
                    lineInfo += LineInfo(currentLineIndex, currentLineWidth, currentLineStartY)
                    currentLineIndex++
                    currentLineWidth = 0f
                    updateCurrentLineStartY = true
                } else {
                    // Not a new line.
                    posX = lastGlyphRunRightEdge
                }
            }
            if (updateCurrentLineStartY)
                currentLineStartY = posY

            // Carry over the y offset if necessary
            if (textRun.carryOverOffsetY) {
                posY += offY
            }

            currentLineWidth = posX

            // Reset font scale
            resetFontForTextRun(font, textRun)
            paintboxFont.end()
            textRunInfo
        }

        lineInfo += LineInfo(currentLineIndex, currentLineWidth, currentLineStartY)

        this.runInfo = runInfo
        this.lineInfo = lineInfo
        this.width = maxPosX
        this.height = -posY + firstCapHeight
        layoutInfoIsInvalid.set(false)
    }

    fun isRunInfoInvalid(): Boolean {
        return (runInfo.isEmpty() && runs.isNotEmpty()) || layoutInfoIsInvalid.get() || runInfo.any { l ->
            l.run.font.currentFontNumber != l.currentFontNumber
        }
    }

    /**
     * Draws this text block. The y value is the baseline value.
     * Same as calling [drawCompressed] with compressText = false.
     */
    fun draw(
        batch: SpriteBatch, x: Float, y: Float, align: TextAlign = TextAlign.LEFT,
        scaleX: Float = 1f, scaleY: Float = 1f, alignAffectsRender: Boolean = false,
    ) {
        drawCompressed(
            batch, x, y, 0f, align, scaleX, scaleY, alignAffectsRender = alignAffectsRender,
            compressText = false
        )
    }

    /**
     * Draws this text block, constraining to the [maxWidth] if necessary.
     * The y value is the baseline value of the first line.
     * The [align] determines how each *line* of text is aligned horizontally.
     * The [batch]'s color is used to tint.
     *
     * @param alignAffectsRender If true, the [align] param will also change the render alignment according to [maxWidth].
     * For example, if [align] is [TextAlign.RIGHT], then the ENTIRE text block will be right-aligned according to
     * the [maxWidth]. If [alignAffectsRender] was false, then the entire block is rendered left-aligned.
     */
    fun drawCompressed(
        batch: SpriteBatch, x: Float, y: Float, maxWidth: Float,
        align: TextAlign = TextAlign.LEFT, scaleX: Float = 1f, scaleY: Float = 1f,
        alignAffectsRender: Boolean = false, compressText: Boolean = true,
    ) {
        computeLayoutsIfNeeded()

        val runInfo = this.runInfo
        if (runInfo.isEmpty())
            return
        if (compressText && maxWidth <= 0f)
            return

        val batchColor: Float = batch.packedColor

        val tint = ColorStack.getAndPush().set(batch.color)
        val requiresTinting = tint.r != 1f || tint.g != 1f || tint.b != 1f || tint.a != 1f

        val globalScaleX: Float =
            scaleX * (if (maxWidth <= 0f || this.width <= 0f || this.width * scaleX < maxWidth) (1f) else (maxWidth / (this.width * scaleX)))
        val globalScaleY: Float = scaleY

        if (globalScaleX <= MathUtils.FLOAT_ROUNDING_ERROR || globalScaleY <= MathUtils.FLOAT_ROUNDING_ERROR || !globalScaleX.isFinite() || !globalScaleY.isFinite()) {
            ColorStack.pop()
            return
        }

        val shouldScaleX = globalScaleX != 1f
        val shouldScaleY = globalScaleY != 1f
        val shouldScaleAny = shouldScaleX || shouldScaleY
        val alignXWidth: Float = if (alignAffectsRender) {
            maxWidth
        } else if (shouldScaleX) {
            this.width * globalScaleX
        } else {
            this.width
        }

        runInfo.forEach { textRunInfo ->
            val paintboxFont = textRunInfo.font
            val font = paintboxFont.begin()
            adjustFontForTextRun(font, textRunInfo.run)

            if (shouldScaleAny) {
                font.scaleMul(globalScaleX, globalScaleY)
            }

            textRunInfo.glyphRunInfo.forEach { glyphRunInfo ->
                val layout = glyphRunInfo.glyphRunAsLayout
                val alignXOffset = when (align) {
                    TextAlign.LEFT -> 0f
                    TextAlign.CENTRE -> (alignXWidth - lineInfo[glyphRunInfo.lineIndex].width * globalScaleX) / 2f
                    TextAlign.RIGHT -> (alignXWidth - lineInfo[glyphRunInfo.lineIndex].width * globalScaleX)
                }

                if (shouldScaleX) {
                    layout.runs.forEach { run ->
                        for (i in 0..<run.xAdvances.size) {
                            run.xAdvances[i] *= globalScaleX
                        }
                    }
                }
                if (shouldScaleY) {
                    layout.runs.forEach { run ->
                        run.y *= globalScaleY
                    }
                }

                if (requiresTinting) {
                    // IntArray pair of ints of start index and ABGR8888 color (Color#toIntBits)
                    val colors = layout.colors
                    val numColors = colors.size / 2
                    val tmpColor = ColorStack.getAndPush()

                    val colorStack: MutableList<Color> = tmpColorStack

                    // For each new colour, push it to ColorStack, then tint it.
                    for (i in 0..<numColors) {
                        val colorsIndex = i * 2 + 1
                        Color.abgr8888ToColor(tmpColor, colors[colorsIndex])
                        colorStack += ColorStack.getAndPush().set(tmpColor)

                        if (tmpColor.r == 1f && tmpColor.g == 1f && tmpColor.b == 1f) {
                            tmpColor.mul(tint)
                        } else {
                            tmpColor.a *= tint.a // Ignore RGB
                        }

                        colors[colorsIndex] = tmpColor.toIntBits()
                    }


                    // Same as else block
                    font.draw(
                        batch, layout,
                        x + (glyphRunInfo.posX) * globalScaleX + alignXOffset,
                        y + (glyphRunInfo.posY) * globalScaleY
                    )


                    // Reset colors from ColorStack
                    for (index in numColors - 1 downTo 0) {
                        val popped = colorStack[index]
                        colors[index * 2 + 1] = popped.toIntBits()
                        ColorStack.pop()
                    }
                    colorStack.clear()

                    // Pop tmpColor
                    ColorStack.pop()
                } else {
                    font.draw(
                        batch, layout,
                        x + (glyphRunInfo.posX) * globalScaleX + alignXOffset,
                        y + (glyphRunInfo.posY) * globalScaleY
                    )
                }

                if (shouldScaleX) {
                    layout.runs.forEach { run ->
                        for (i in 0..<run.xAdvances.size) {
                            run.xAdvances[i] /= globalScaleX
                        }
                    }
                }
                if (shouldScaleY) {
                    layout.runs.forEach { run ->
                        run.y /= globalScaleY
                    }
                }
            }

            if (shouldScaleAny) {
                font.scaleMul(1f / globalScaleX, 1f / globalScaleY)
            }

            resetFontForTextRun(font, textRunInfo.run)
            paintboxFont.end()
        }

        ColorStack.pop()

        batch.packedColor = batchColor
    }
    
    override fun toString(): String {
        return "runs=[${runs}]"
    }
}