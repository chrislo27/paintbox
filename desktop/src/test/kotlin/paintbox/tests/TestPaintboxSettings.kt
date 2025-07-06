package paintbox.tests

import paintbox.IPaintboxSettings
import paintbox.ResizeAction
import paintbox.logging.Logger
import paintbox.util.Version
import paintbox.util.WindowSize
import java.io.File


data class TestPaintboxSettings(
    override val launchArguments: List<String>,
    override val logger: Logger, override val logToFile: File?,
    override val version: Version,
    override val emulatedSize: WindowSize, override val resizeAction: ResizeAction,
    override val minimumSize: WindowSize,
) : IPaintboxSettings
