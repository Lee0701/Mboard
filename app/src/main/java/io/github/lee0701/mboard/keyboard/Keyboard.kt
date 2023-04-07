package io.github.lee0701.mboard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.*
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.KeyType
import kotlin.math.roundToInt

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {
    private val handler = Handler(Looper.getMainLooper())

    private var keyPopup: KeyPopup? = null

    @SuppressLint("ClickableViewAccessibility")
    fun initView(context: Context, theme: Theme, listener: Listener): ViewWrapper {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val longPressDuration = sharedPreferences.getInt("behaviour_longpress_duration", 1000).toLong()
        val repeatInterval = sharedPreferences.getInt("behaviour_repeat_interval", 50).toLong()
        val showKeyPopups = sharedPreferences.getBoolean("behaviour_show_popups", true)

        val wrappedContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)

        val rowViewWrappers = mutableListOf<Row.ViewWrapper>()
        val view = LayoutInflater.from(wrappedContext).inflate(R.layout.keyboard, null, false).apply {
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@Keyboard.height, wrappedContext.resources.displayMetrics).toInt()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height
            )
            rows.forEach { row ->
                val rowViewWrapper = row.initView(context, theme)
                rowViewWrappers += rowViewWrapper
                (this as ViewGroup).addView(rowViewWrapper.view)
            }
        }

        keyPopup = KeyPopup(context)

        val keyViewWrappers = rowViewWrappers.flatMap { it.keys }
        keyViewWrappers.forEach { key ->
            key.view.setOnTouchListener { v, event ->
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if(showKeyPopups &&
                            (key.key.type == KeyType.Alphanumeric || key.key.type == KeyType.AlphanumericAlt)) {
                            keyPopup?.apply {
                                val row = rowViewWrappers.find { key in it.keys } ?: return@apply
                                val x = key.view.x.roundToInt() + key.view.width / 2
                                val y = row.view.y.roundToInt() + row.view.height / 2
                                show(view, key, x, y)
                            }
                        } else {
                            keyPopup?.cancel()
                        }
                        fun repeater() {
                            listener.onKeyClick(key.key.code, key.key.output)
                            handler.postDelayed({ repeater() }, repeatInterval)
                        }
                        handler.postDelayed({
                            if(key.key.repeatable) repeater()
                        }, longPressDuration)
                        listener.onKeyDown(key.key.code, key.key.output)
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacksAndMessages(null)
                        keyPopup?.hide()
                        listener.onKeyUp(key.key.code, key.key.output)
                    }
                }
                false
            }
            key.view.setOnClickListener {
                listener.onKeyClick(key.key.code, key.key.output)
            }
        }
        return ViewWrapper(this, view, rowViewWrappers, keyViewWrappers)
    }

    data class ViewWrapper(
        val keyboard: Keyboard,
        val view: View,
        val rows: List<Row.ViewWrapper>,
        val keys: List<Key.ViewWrapper>,
    ) {
        val keyMap = keys.associateBy { it.key.code }
    }

    interface Listener {
        fun onKeyClick(code: Int, output: String?)
        fun onKeyDown(code: Int, output: String?)
        fun onKeyUp(code: Int, output: String?)
    }
}
