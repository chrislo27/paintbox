package paintbox.tests

import paintbox.IPaintboxSettings
import paintbox.ResizeAction
import paintbox.util.Version
import paintbox.util.WindowSize


data class TestPaintboxSettings(
    override val launchArguments: List<String>,
    override val loggerSettings: IPaintboxSettings.ILoggerSettings,
    override val version: Version,
    override val emulatedSize: WindowSize, override val resizeAction: ResizeAction,
    override val minimumSize: WindowSize,
) : IPaintboxSettings
