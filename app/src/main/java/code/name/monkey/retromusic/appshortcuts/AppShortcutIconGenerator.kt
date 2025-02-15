package code.name.monkey.retromusic.appshortcuts

import android.content.Context
import android.graphics.drawable.Icon
import android.graphics.drawable.LayerDrawable
import android.os.Build
import android.util.TypedValue
import androidx.annotation.RequiresApi
import androidx.core.graphics.drawable.toBitmap
import code.name.monkey.appthemehelper.ThemeStore
import code.name.monkey.retromusic.R
import code.name.monkey.retromusic.extensions.getTintedDrawable
import code.name.monkey.retromusic.util.PreferenceUtil

@RequiresApi(Build.VERSION_CODES.N_MR1)
object AppShortcutIconGenerator {
    fun generateThemedIcon(context: Context, iconId: Int): Icon {
        return if (PreferenceUtil.isColoredAppShortcuts) {
            generateUserThemedIcon(context, iconId)
        } else {
            generateDefaultThemedIcon(context, iconId)
        }
    }

    private fun generateDefaultThemedIcon(context: Context, iconId: Int): Icon {
        return generateThemedIcon(
            context,
            iconId,
            context.getColor(R.color.app_shortcut_default_foreground),
            context.getColor(R.color.app_shortcut_default_background)
        )
    }

    private fun generateUserThemedIcon(context: Context, iconId: Int): Icon {
        val typedColorBackground = TypedValue()
        context.theme.resolveAttribute(android.R.attr.colorBackground, typedColorBackground, true)

        return generateThemedIcon(
            context, iconId, ThemeStore.accentColor(context), typedColorBackground.data
        )
    }

    private fun generateThemedIcon(
        context: Context,
        iconId: Int,
        foregroundColor: Int,
        backgroundColor: Int,
    ): Icon {
        val vectorDrawable = context.getTintedDrawable(iconId, foregroundColor)
        val backgroundDrawable =
            context.getTintedDrawable(R.drawable.ic_app_shortcut_background, backgroundColor)
        val layerDrawable = LayerDrawable(arrayOf(backgroundDrawable, vectorDrawable))
        return Icon.createWithBitmap(layerDrawable.toBitmap())
    }
}
