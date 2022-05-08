package paintbox.transition

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import paintbox.PaintboxGame
import paintbox.PaintboxScreen
import kotlin.math.absoluteValue


/**
 * A one-use transition screen.
 *
 * During a transition, only the render method is called for the entry and destination screens.
 *
 */
open class TransitionScreen(override val main: PaintboxGame,
                            val entryScreen: Screen?, val destScreen: Screen?,
                            entryTransition: Transition?, destTransition: Transition?)
    : PaintboxScreen() {
    
    companion object {
        private val NO_OP_CALLBACK: () -> Unit = {}
    }
    
    enum class Substate {
        PRE_ENTRY,
        DURING_ENTRY,
        DURING_DEST,
        FINISHED
    }

    val entryTransition: Transition = entryTransition ?: Transition.EMPTY
    val destTransition: Transition = destTransition ?: Transition.EMPTY

    val duration: Float = (this.entryTransition.duration + this.destTransition.duration).absoluteValue
    var timeElapsed: Float = 0f
        private set
    
    var onStart: () -> Unit = NO_OP_CALLBACK
    var onEntryStart: () -> Unit = NO_OP_CALLBACK
    var onEntryEnd: () -> Unit = NO_OP_CALLBACK
    var onDestStart: () -> Unit = NO_OP_CALLBACK
    var onDestEnd: () -> Unit = NO_OP_CALLBACK
    var onFinished: () -> Unit = NO_OP_CALLBACK

    /**
     * The total percentage from 0.0 to 1.0 of the transition state.
     */
    val percentageTotal: Float
        get() = if (duration == 0f) 1f else (timeElapsed / duration).coerceIn(0f, 1f)

    /**
     * The total percentage from 0.0 to 1.0 of the ENTRY transition.
     */
    val percentageEntry: Float
        get() = if (this.entryTransition.duration == 0f) {
            1f
        } else {
            (timeElapsed / this.entryTransition.duration).coerceIn(0f, 1f)
        }

    /**
     * The total percentage from 0.0 to 1.0 of the DESTINATION transition.
     */
    val percentageDest: Float
        get() = if (this.destTransition.duration == 0f) {
            1f
        } else {
            ((timeElapsed - this.entryTransition.duration) / this.destTransition.duration).coerceIn(0f, 1f)
        }
    val percentageCurrent: Float
        get() = if (doneEntry) percentageDest else percentageEntry
    val done: Boolean
        get() = percentageTotal >= 1.0f
    val doneEntry: Boolean
        get() = percentageEntry >= 1.0f

    private var substate: Substate = Substate.PRE_ENTRY
    private var lastScreen: Screen? = entryScreen
    private var skipTimeUpdateForNFrames: Int = 0

    override fun render(delta: Float) {
        super.render(delta)

        // Render transition
        val transition = (if (doneEntry) destTransition else entryTransition)
        val screen = (if (doneEntry) destScreen else entryScreen)

        if (substate == Substate.PRE_ENTRY) {
            skipTimeUpdateForNFrames = 1
            this.substate = Substate.DURING_ENTRY
            onStart.invoke()
            onEntryStart.invoke()
        }
        if (substate == Substate.DURING_ENTRY) {
            if (doneEntry) {
                this.substate = Substate.DURING_DEST
                destScreen?.resize(Gdx.graphics.width, Gdx.graphics.height)
                onEntryEnd.invoke()
                onDestStart.invoke()
                skipTimeUpdateForNFrames = 1
            }
        }
        
        val lastScreenDiffers = lastScreen != screen
        if (lastScreenDiffers) {
            (screen as? PaintboxScreen)?.showTransition() ?: (screen?.show())
            lastScreen = screen
        }
        
        transition.render(this) { screen?.render(delta) }

        if (transition.overrideDone) {
            timeElapsed = if (doneEntry) {
                duration
            } else {
                entryTransition.duration
            }
        }

        if (done) {
            if (substate == Substate.DURING_DEST) {
                this.substate = Substate.FINISHED
                onDestEnd.invoke()
                onFinished.invoke()
            }
            main.screen = destScreen
            dispose()
        } else {
            if (!lastScreenDiffers) {
                if (skipTimeUpdateForNFrames > 0) {
                    skipTimeUpdateForNFrames--
                } else {
                    timeElapsed += delta
                }
            }
        }
    }

    override fun dispose() {
        entryTransition.dispose()
        destTransition.dispose()
    }

    override fun getDebugString(): String? {
        return """entryScreen: ${entryScreen?.javaClass?.name}
destScreen: ${destScreen?.javaClass?.name}

entryTransition: ${entryTransition.javaClass.name}
destTransition: ${destTransition.javaClass.name}
"""
    }
}