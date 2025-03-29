package code.name.monkey.retromusic.views.upload


import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import code.name.monkey.appthemehelper.ThemeStore

class LinearProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ThemeStore.accentColor(context)
    }
    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, android.R.color.darker_gray)
    }
    private var progress: Float = 0f

    fun setProgress(value: Float) {
        progress = value.coerceIn(0f, 100f)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()
        val radius = height / 2

        val backgroundRect = RectF(0f, 0f, width, height)
        canvas.drawRoundRect(backgroundRect, radius, radius, backgroundPaint)

        val progressWidth = width * (progress / 100)
        val progressRect = RectF(0f, 0f, progressWidth, height)
        canvas.drawRoundRect(progressRect, radius, radius, progressPaint)
    }
}
