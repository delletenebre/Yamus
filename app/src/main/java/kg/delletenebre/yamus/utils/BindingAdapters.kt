package kg.delletenebre.yamus.utils

import android.graphics.Color
import android.graphics.drawable.Animatable
import android.graphics.drawable.Drawable
import android.net.Uri
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.mikhaellopez.circularprogressbar.CircularProgressBar
import kg.delletenebre.yamus.GlideApp
import kg.delletenebre.yamus.R
import kg.delletenebre.yamus.api.YandexApi


object BindingAdapters {
    @JvmStatic @BindingAdapter("glideImgSrc")
    fun ImageView.glideImgSrc(imageUrl: String) {
        Glide.with(this.context)
                .load(YandexApi.getImageUrl(imageUrl, 200))
                .into(this)
    }

    @JvmStatic @BindingAdapter("loadImgUri")
    fun ImageView.loadImgUri(imageUri: Uri) {
        when {
            imageUri == Uri.EMPTY -> this.setImageResource(R.drawable.default_album_art)
            imageUri.scheme == "https" -> GlideApp.with(this.context)
                    .load(imageUri)
                    .placeholder(R.drawable.default_album_art)
                    .into(this)
            else -> this.setImageURI(imageUri)
        }
    }

    @JvmStatic @BindingAdapter("loadImg")
    fun ImageView.loadImg(url: String) {
        GlideApp.with(this.context)
                .load(url)
                .placeholder(R.drawable.default_album_art)
                .into(this)
    }

    @JvmStatic @BindingAdapter("glideCircle")
    fun ImageView.glideCircle(imageUrl: String) {
        Glide.with(this.context)
                .load(YandexApi.getImageUrl(imageUrl, 200))
                .apply(RequestOptions.circleCropTransform())
                .into(this)
    }

    @JvmStatic @BindingAdapter("glideImgSrc", "glideImgSize")
    fun ImageView.glideImgSrc(imageUrl: String, size: Int) {
        Glide.with(this.context)
                .load(YandexApi.getImageUrl(imageUrl, size))
                .into(this)
    }

    @JvmStatic @BindingAdapter("backgroundColor")
    fun ImageView.backgroundColor(color: String) {
        this.setBackgroundColor(Color.parseColor(color))
    }

    @JvmStatic @BindingAdapter("backgroundColor")
    fun FrameLayout.backgroundColor(color: String) {
        this.setBackgroundColor(Color.parseColor(color))
    }

    @JvmStatic @BindingAdapter("cpb_progress")
    fun CircularProgressBar.cpb_progress(progress: Int) {
        this.progress = progress.toFloat()
    }

    @JvmStatic @BindingAdapter("cpb_indeterminate_mode")
    fun CircularProgressBar.cpb_indeterminate_mode(enabled: Boolean) {
        this.indeterminateMode = enabled
    }

    @JvmStatic @BindingAdapter("animateDrawable")
    fun ImageView.animateDrawable(backDrawable: Drawable?) {
        this.setImageDrawable(backDrawable)
        val drawable = this.drawable as Animatable
        drawable.start()
    }

    @JvmStatic @BindingAdapter("animateDrawable")
    fun ImageView.animateDrawable(id: Int?) {
        if (id != null) {
            this.setImageDrawable(ContextCompat.getDrawable(this.context, id))
            val drawable = this.drawable as Animatable
            drawable.start()
        }
    }

    @JvmStatic @BindingAdapter("drawableId")
    fun ImageView.drawableId(id: Int?) {
        if (id != null) {
            this.setImageDrawable(ContextCompat.getDrawable(this.context, id))
        }
    }

//    @JvmStatic @BindingAdapter("layoutWidth")
//    fun setLayoutWidth(layout: ViewGroup, dimen: Float) {
//        val layoutParams = layout.layoutParams
//        layoutParams.width = dimen.toInt()
//        layout.layoutParams = layoutParams
//    }
//
    @JvmStatic @BindingAdapter("imageSize")
    fun ImageView.imageSize(dimen: Float) {
        val layoutParams = this.layoutParams
        layoutParams.width = dimen.toInt()
        layoutParams.height = dimen.toInt()
        this.layoutParams = layoutParams
    }
}