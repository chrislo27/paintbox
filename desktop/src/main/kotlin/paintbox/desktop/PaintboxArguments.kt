package paintbox.desktop

import com.beust.jcommander.Parameter
import paintbox.IPaintboxArguments


open class PaintboxArguments : IPaintboxArguments {

    @Parameter(names = ["--help", "-h", "-?"], description = "Prints this usage menu.", help = true)
    override var printHelp: Boolean = false

    // -----------------------------------------------------------

    @Parameter(
        names = ["--fps"],
        description = "Manually sets the target FPS. Must be greater than or equal to zero. If zero, the framerate is unbounded."
    )
    override var fps: Int? = null

    @Parameter(names = ["--vsync"], description = "Enables/disables VSync (vertical sync).", arity = 1)
    override var vsync: Boolean? = null

}