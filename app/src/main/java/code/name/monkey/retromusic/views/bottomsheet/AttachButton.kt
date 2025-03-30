package code.name.monkey.retromusic.views.bottomsheet

import android.animation.Animator
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.text.TextUtils
import android.util.TypedValue
import android.view.Gravity
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RawRes
import code.name.monkey.retromusic.util.AndroidUtilities.dp
import com.airbnb.lottie.LottieAnimationView


class AttachButton(context: Context) : FrameLayout(context) {

    private lateinit var textView: TextView
    private lateinit var imageView: LottieAnimationView
    private var checked = false
    private var checkedState = 0f
    private var checkAnimator: Animator? = null
    private var currentId = 0
    private val selectedId = 1
    private val attachItemSize = dp(85)
    private val attachButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        imageView = LottieAnimationView(context).apply {
            scaleType = ImageView.ScaleType.CENTER
            layoutParams =
                LayoutParams(dp(32), dp(32), Gravity.CENTER_HORIZONTAL or Gravity.TOP).apply {
                    setMargins(0, dp(18), 0, 0)
                }
        }
        addView(imageView)

        textView = TextView(context).apply {
            maxLines = 2
            gravity = Gravity.CENTER
            ellipsize = TextUtils.TruncateAt.END
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 12f)
            setLineSpacing(-dp(2).toFloat(), 1.0f)
            layoutParams = LayoutParams(
                MATCH_PARENT,
                WRAP_CONTENT,
                Gravity.CENTER_HORIZONTAL or Gravity.TOP
            ).apply {
                setMargins(0, dp(62), 0, 0)
            }
        }
        addView(textView)
    }

    fun updateCheckedState(animate: Boolean) {
        if (checked == (currentId == selectedId)) return
        checked = currentId == selectedId

        checkAnimator?.cancel()

        if (animate) {
            if (checked) {
                imageView.progress = 0.0f
                imageView.playAnimation()
            }
            checkAnimator =
                ObjectAnimator.ofFloat(this, "checkedState", if (checked) 1f else 0f).apply {
                    duration = 200
                    start()
                }
        } else {
            imageView.cancelAnimation()
            imageView.progress = 0.0f
            setCheckedState(if (checked) 1f else 0f)
        }
    }

    fun setCheckedState(state: Float) {
        checkedState = state
        imageView.scaleX = 1.0f - 0.06f * state
        imageView.scaleY = 1.0f - 0.06f * state
        textView.setTextColor(Color.GRAY)
        invalidate()
    }

    fun setTextAndIcon(
        id: Int,
        text: CharSequence?,
        @RawRes drawable: Int?,
        background: Int,
        textColor: Int
    ) {
        currentId = id
        textView.text = text
        drawable?.let { imageView.setAnimation(it) } ?: imageView.cancelAnimation()
        textView.setTextColor(textColor)
    }

    override fun onDraw(canvas: Canvas) {
        val scale = imageView.scaleX + 0.06f * checkedState
        val radius = dp(23) * scale
        val cx = imageView.left + imageView.measuredWidth / 2f
        val cy = imageView.top + imageView.measuredWidth / 2f

        attachButtonPaint.color = Color.BLACK
        attachButtonPaint.style = Paint.Style.STROKE
        attachButtonPaint.strokeWidth = dp(3) * scale
        attachButtonPaint.alpha = (255f * checkedState).toInt()
        canvas.drawCircle(cx, cy, radius - 0.5f * attachButtonPaint.strokeWidth, attachButtonPaint)

        attachButtonPaint.alpha = 255
        attachButtonPaint.style = Paint.Style.FILL
        canvas.drawCircle(cx, cy, radius - dp(5) * checkedState, attachButtonPaint)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            MeasureSpec.makeMeasureSpec(attachItemSize, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(dp(84), MeasureSpec.EXACTLY)
        )
    }
}
