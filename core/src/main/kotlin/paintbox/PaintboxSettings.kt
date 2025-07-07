package paintbox

import paintbox.logging.Logger
import paintbox.util.Version
import paintbox.util.WindowSize
import java.io.File


interface IPaintboxSettings {

    val launchArguments: List<String>

    val loggerSettings: ILoggerSettings

    val version: Version

    val emulatedSize: WindowSize
    val resizeAction: ResizeAction
    val minimumSize: WindowSize


    interface ILoggerSettings {

        val logger: Logger
        val teeConsoleToFile: File?

        class Impl(
            override val logger: Logger,
            override val teeConsoleToFile: File?,
        ) : ILoggerSettings
    }

    class Impl(
        override val launchArguments: List<String>,
        override val loggerSettings: ILoggerSettings,
        override val version: Version,
        override val emulatedSize: WindowSize, override val resizeAction: ResizeAction,
        override val minimumSize: WindowSize,
    ) : IPaintboxSettings
}
