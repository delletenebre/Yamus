package kg.delletenebre.yamus

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule


@GlideModule
class YamusGlide : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSizeBytes = 1024 * 1024 * 25 // 25 MB
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, diskCacheSizeBytes.toLong()))
    }
}