package paintbox

import paintbox.IPaintboxSettings.ILoggerSettings
import paintbox.logging.Logger
import paintbox.util.Version
import paintbox.util.WindowSize
import java.io.File


/**
 * The action to take when the screen is resized.
 */
enum class ResizeAction {

    /**
     * The window can be adjusted to any size. Fonts do not reload.
     */
    ANY_SIZE,

    /**
     * The game is always emulated at a certain set of dimensions. Fonts do not reload.
     */
    LOCKED,

    /**
     * The window can be adjusted to any size, and the internal camera will attempt to keep a maximum fit of the
     * provided aspect ratio. Fonts **do** reload.
     */
    KEEP_ASPECT_RATIO

}

/**
 * Legacy version that used to be the data class PaintboxSettings. This class's constructor will not change.
 */
@Suppress("CanBePrimaryConstructorProperty")
class LegacyPaintboxSettings(
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
