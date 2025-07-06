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

    /**
     * Legacy version that used to be the data class PaintboxSettings. This class's constructor will not change.
     */
    @Suppress("CanBePrimaryConstructorProperty")
    class LegacyImpl(
        launchArguments: List<String>,
        logger: Logger, logToFile: File?,
        version: Version,
        emulatedSize: WindowSize, resizeAction: ResizeAction,
        minimumSize: WindowSize,
    ) : IPaintboxSettings {

        override val launchArguments: List<String> = launchArguments

        override val loggerSettings: ILoggerSettings = ILoggerSettings.Impl(
            logger = logger,
            teeConsoleToFile = logToFile
        )

        override val version: Version = version

        override val emulatedSize: WindowSize = emulatedSize
        override val resizeAction: ResizeAction = resizeAction
        override val minimumSize: WindowSize = minimumSize
    }
}
