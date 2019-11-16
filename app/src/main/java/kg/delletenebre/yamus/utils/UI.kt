package kg.delletenebre.yamus.utils

import android.content.Context
import android.util.TypedValue
import android.view.Menu
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.forEach
import kg.delletenebre.yamus.R

object UI {
    fun setMenuIconsColor(context: Context, menu: Menu) {
        val typedValue = TypedValue()
        context.theme.resolveAttribute(R.attr.colorControlNormal, typedValue, true)
        val color = ContextCompat.getColor(context, typedValue.resourceId)

        menu.forEach {
            val icon = DrawableCompat.wrap(it.icon)
            DrawableCompat.setTint(icon.mutate(), color)
        }

    }
}