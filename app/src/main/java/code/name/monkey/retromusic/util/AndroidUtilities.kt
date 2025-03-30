package code.name.monkey.retromusic.util

import android.content.res.Resources

object AndroidUtilities {
    fun dp(value: Float): Int {
        return (value * Resources.getSystem().displayMetrics.density).toInt()
    }

    fun dp(value: Int): Int {
        return dp(value.toFloat())
    }
}