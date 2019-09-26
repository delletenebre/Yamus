package kg.delletenebre.yamus.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import kg.delletenebre.yamus.api.YandexApi

object BindingAdapters {
    @JvmStatic @BindingAdapter("glideImgSrc")
    fun ImageView.glideImgSrc(imageUrl: String) {
        Glide.with(this.context)
            .load(YandexApi.getImage(imageUrl, 200))
            .into(this)
    }

    @JvmStatic @BindingAdapter("glideImgSrc", "glideImgSize")
    fun ImageView.glideImgSrc(imageUrl: String, size: Int) {
        Glide.with(this.context)
                .load(YandexApi.getImage(imageUrl, size))
                .into(this)
    }
}