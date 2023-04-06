package io.github.lee0701.mboard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.FrameLayout
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.KeyboardBinding
import kotlin.math.roundToInt

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {

    private val handler = Handler(Looper.getMainLooper())

    // TODO: Read these values from preference
    private val repeatStartDelay = 500L
    private val repeatDelay = 50L

    private var keyPopup: KeyPopup? = null

    @SuppressLint("ClickableViewAccessibility")
    fun initView(context: Context, theme: Theme, listener: Listener): ViewWrapper {
        val wrappedContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboard)
        val rowViewWrappers = mutableListOf<Row.ViewWrapper>()
        val binding = KeyboardBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@Keyboard.height, wrappedContext.resources.displayMetrics).toInt()
            root.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height
            )
            rows.forEach { row ->
                val rowViewWrapper = row.initView(context, theme)
                rowViewWrappers += rowViewWrapper
                root.addView(rowViewWrapper.binding.root)
            }
        }

        keyPopup = KeyPopup(context)

        val keyViewWrappers = rowViewWrappers.flatMap { it.keys }
        keyViewWrappers.forEach { key ->
            key.binding.root.setOnTouchListener { v, event ->
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if(key.key.type == Key.Type.Alphanumeric || key.key.type == Key.Type.AlphanumericAlt) {
                            keyPopup?.apply {
                                val row = rowViewWrappers.find { key in it.keys } ?: return@apply
                                val x = key.binding.root.x.roundToInt() + key.binding.root.width / 2
                                val y = row.binding.root.y.roundToInt() + row.binding.root.height / 2
                                show(binding.root, key, x, y)
                            }
                        } else {
                            keyPopup?.cancel()
                        }
                        fun repeater() {
                            listener.onKeyClick(key.key.code, key.key.output)
                            handler.postDelayed({ repeater() }, repeatDelay)
                        }
                        handler.postDelayed({
                            if(key.key.repeatable) repeater()
                        }, repeatStartDelay)
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
            key.binding.root.setOnClickListener {
                listener.onKeyClick(key.key.code, key.key.output)
            }
        }
        return ViewWrapper(this, binding, rowViewWrappers, keyViewWrappers)
    }

    data class ViewWrapper(
        val keyboard: Keyboard,
        val binding: KeyboardBinding,
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
