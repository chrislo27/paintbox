package paintbox

import paintbox.logging.Logger
import paintbox.util.Version
import paintbox.util.WindowSize
import java.io.File


interface IPaintboxSettings {

    val launchArguments: List<String>

    val logger: Logger
    val logToFile: File?

    val version: Version

    val emulatedSize: WindowSize
    val resizeAction: ResizeAction
    val minimumSize: WindowSize
    
    class Impl(
        override val launchArguments: List<String>,
        override val logger: Logger, override val logToFile: File?,
        override val version: Version,
        override val emulatedSize: WindowSize, override val resizeAction: ResizeAction,
        override val minimumSize: WindowSize,
    ) : IPaintboxSettings
}
