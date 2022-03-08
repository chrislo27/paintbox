package paintbox.filechooser

import java.io.File


/**
 * Callback-based async file chooser with support for file filters ([FileExtFilter]).
 * 
 * The callback may be called on a separate thread than what the original function was called on.
 * 
 * **Operations:**
 *   * Save to a file
 *   * Open a file
 *   * Open a set of files
 *   * Select a folder
 * 
 */
interface IFileDialog {

    /**
     * Opens a "save file" dialog and calls the [callback] with the given [File] or null if the
     * operation was cancelled.
     */
    fun saveFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit)
    
    /**
     * Opens an "open file" dialog and calls the [callback] with the given [File] or null if the
     * operation was cancelled.
     */
    fun openFile(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (File?) -> Unit)
    
    /**
     * Opens an "open file" dialog that accepts multiple files and
     * calls the [callback] with the given list of [File]s or an empty list if the operation was cancelled.
     */
    fun openMultipleFiles(title: String, defaultFile: File?, filter: FileExtFilter?, callback: (List<File>) -> Unit)

    /**
     * Opens a "select folder" dialog and calls the [callback] with the given [File] or null if the
     * operation was cancelled.
     */
    fun selectFolder(title: String, defaultDir: File?, callback: (File?) -> Unit)
    
}