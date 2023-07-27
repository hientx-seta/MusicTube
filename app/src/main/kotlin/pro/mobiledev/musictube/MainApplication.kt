package pro.mobiledev.musictube

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import pro.mobiledev.musictube.enums.CoilDiskCacheMaxSize
import pro.mobiledev.musictube.utils.coilDiskCacheMaxSizeKey
import pro.mobiledev.musictube.utils.getEnum
import pro.mobiledev.musictube.utils.preferences

class MainApplication : Application(), ImageLoaderFactory {
    override fun onCreate() {
        super.onCreate()
        DatabaseInitializer()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .respectCacheHeaders(false)
            .diskCache(
                DiskCache.Builder()
                    .directory(cacheDir.resolve("coil"))
                    .maxSizeBytes(
                        preferences.getEnum(
                            coilDiskCacheMaxSizeKey,
                            CoilDiskCacheMaxSize.`128MB`
                        ).bytes
                    )
                    .build()
            )
            .build()
    }
}
