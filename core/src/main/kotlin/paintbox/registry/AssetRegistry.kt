package paintbox.registry

import com.badlogic.gdx.assets.AssetLoaderParameters
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.assets.loaders.TextureLoader
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.utils.Array
import com.badlogic.gdx.utils.Disposable
import paintbox.lazysound.LazySound
import paintbox.lazysound.LazySoundLoader
import paintbox.packing.PackedSheet
import paintbox.packing.PackedSheetLoader


open class AssetRegistryInstance : Disposable {

    enum class LoadState {
        NONE, LOADING, DONE
    }

    val manager: AssetManager = AssetManager()
    val unmanagedAssets: MutableMap<String, Any> = mutableMapOf()
    val assetMap: Map<String, String> = mutableMapOf()

    private val assetLoaders: MutableList<IAssetLoader> = mutableListOf()

    private var disposed: Boolean = false
    private var loadingState: LoadState = LoadState.NONE

    init {
        manager.setLoader(LazySound::class.java, LazySoundLoader(manager.fileHandleResolver))
        manager.setLoader(PackedSheet::class.java, PackedSheetLoader(manager.fileHandleResolver))

        // No-op asset loader
        addAssetLoader(object : IAssetLoader {
            override fun addManagedAssets(manager: AssetManager) {}
            override fun addUnmanagedAssets(assets: MutableMap<String, Any>) {}
        })
    }

    /**
     * Creates a mapping from [key] to the filename [file].
     * @throws IllegalArgumentException If the key has already been bound
     */
    fun bindAsset(key: String, file: String): Pair<String, String> {
        if (assetMap.containsKey(key)) {
            throw IllegalArgumentException("$key has already been bound to ${assetMap[key]}")
        }

        (assetMap as MutableMap)[key] = file
        return key to file
    }

    /**
     * Loads an asset and binds [key] to [file].
     */
    inline fun <reified T> loadAsset(key: String, file: String, params: AssetLoaderParameters<T>? = null) {
        manager.load(bindAsset(key, file).second, T::class.java, params)
    }

    /**
     * Loads an asset where the key is also the filename.
     */
    inline fun <reified T> loadAssetNoFile(key: String, params: AssetLoaderParameters<T>?) {
        manager.load(bindAsset(key, key).second, T::class.java, params)
    }

    /**
     * Unloads the asset by the key. If the asset doesn't exist or isn't loaded, nothing happens.
     */
    fun unloadAsset(key: String) {
        val filename = assetMap[key]
        if (filename != null) {
            (assetMap as MutableMap).remove(key)
            if (unmanagedAssets.containsKey(key)) {
                val item = unmanagedAssets.remove(key)
                if (item != null) {
                    (item as? Disposable)?.dispose()
                }
            } else if (manager.isLoaded(filename)) {
                manager.unload(filename)
            }
        }
    }

    fun addAssetLoader(loader: IAssetLoader) {
        assetLoaders += loader
        val map = mutableMapOf<String, Any>()
        loader.addUnmanagedAssets(map)
        unmanagedAssets.putAll(map)
    }

    fun load(delta: Float): Float {
        if (disposed) {
            return 0f
        }

        if (loadingState == LoadState.NONE) {
            loadingState = LoadState.LOADING

            assetLoaders.forEach {
                it.addManagedAssets(manager)
            }
        }

        if (manager.update((delta * 1000).coerceIn(0f, Int.MAX_VALUE.toFloat()).toInt())) {
            loadingState = LoadState.DONE
        }

        return manager.progress
    }

    fun loadBlocking() {
        @Suppress("ControlFlowWithEmptyBody")
        while (load(Int.MAX_VALUE.toFloat()) < 1f);
    }

    operator fun contains(key: String): Boolean {
        return key in unmanagedAssets || key in assetMap
    }

    inline fun <reified T> containsAsType(key: String): Boolean {
        if (!contains(key))
            return false

        return (unmanagedAssets[key] as T?) != null || manager.isLoaded(assetMap[key], T::class.java)
    }

    inline operator fun <reified T> get(key: String): T {
        val unmanaged = (unmanagedAssets[key] as T?)
        if (unmanaged != null) {
            return unmanaged
        }
        if (assetMap[key] == null) {
            error("Key not found in mappings: $key")
        }
        if (!manager.isLoaded(assetMap[key], T::class.java)) {
            error("Asset not loaded/found: ${T::class.java.name} - $key")
        }

        return manager.get(assetMap[key], T::class.java)
    }

    /**
     * Gets an asset by its [key] without doing type checking.
     */
    fun fastGet(key: String): Any? {
        val mappedKey = assetMap[key] ?: return null
        if (unmanagedAssets[mappedKey] != null) return unmanagedAssets[key]
        return manager.get(mappedKey)
    }

    /**
     * Unloads all assets and resets the state of this [AssetRegistryInstance] so it can be loaded again.
     */
    fun unloadAllAssets() {
        unmanagedAssets.values.filterIsInstance(Disposable::class.java).forEach(Disposable::dispose)
        unmanagedAssets.clear()
        manager.clear()
        (assetMap as MutableMap).clear()
        loadingState = LoadState.NONE
    }

    override fun dispose() {
        disposed = true
        unloadAllAssets()
        manager.dispose()
    }


    fun stopAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::stop)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded).forEach { it.sound.stop() }
    }

    fun pauseAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::pause)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded).forEach { it.sound.pause() }
    }

    fun resumeAllSounds() {
        manager.getAll(Sound::class.java, Array()).toList().forEach(Sound::resume)
        manager.getAll(LazySound::class.java, Array()).toList().filter(LazySound::isLoaded)
            .forEach { it.sound.resume() }
    }
}

/**
 * Holds all the global assets needed for a game and can be easily disposed of at the end.
 */
object AssetRegistry : AssetRegistryInstance()

interface IAssetLoader {

    fun addManagedAssets(manager: AssetManager)

    fun addUnmanagedAssets(assets: MutableMap<String, Any>)

    fun linearTexture(): TextureLoader.TextureParameter = TextureLoader.TextureParameter().apply {
        this.magFilter = Texture.TextureFilter.Linear
        this.minFilter = Texture.TextureFilter.Linear
    }

}