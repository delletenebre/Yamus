package kg.delletenebre.yamus.views

import android.content.Context
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import kg.delletenebre.yamus.R

class PersonalPlaylistView(context: Context): LinearLayout(context) {
    var imageView: ImageView
    var titleView: TextView
    var subtitleView: TextView

    init {
        val root = inflate(context, R.layout.view_personal_playlist, this)
        imageView = root.findViewById(R.id.image)
        titleView  = root.findViewById(R.id.title)
        subtitleView = root.findViewById(R.id.subtitle)
//        val attributes = context.obtainStyledAttributes(attrs, R.styleable.BenefitView)
//        imageView.setImageDrawable(attributes.getDrawable(R.styleable.BenefitView_image))
//        textView.text = attributes.getString(R.styleable.BenefitView_text)
//        attributes.recycle()

    }

    fun setTitle(title: String) {
        titleView.text = title
    }

    fun setSubtitle(subtitle: String) {
        subtitleView.text = subtitle
    }

    fun setImage(url: String) {
        Glide.with(context)
                .load(url)
//                .centerCrop()
//                .placeholder(R.drawable.default_image)
                .into(imageView)
    }
}