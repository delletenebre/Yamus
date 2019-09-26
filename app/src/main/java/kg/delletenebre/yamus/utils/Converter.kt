package kg.delletenebre.yamus.utils

import android.content.Context
import android.util.TypedValue
import kotlin.math.roundToInt


object Converter {
    fun dp2px(dp: Int, context: Context): Int {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp.toFloat(), context.resources.displayMetrics).toInt()
    }

    fun px2dp(px: Int, context: Context): Int {
        return (px / context.resources.displayMetrics.density).roundToInt()
    }

}