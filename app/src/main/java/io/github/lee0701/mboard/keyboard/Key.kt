package io.github.lee0701.mboard.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import androidx.annotation.StyleRes
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding

data class Key(
    val code: Int,
    val output: String?,
    val label: String? = output,
    val icon: Int? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: Type = Type.Alphanumeric,
) {
    val handler = Handler(Looper.getMainLooper())

    // TODO: Read these values from preference
    private val repeatStartDelay = 500L
    private val repeatDelay = 50L

    @SuppressLint("ClickableViewAccessibility")
    fun initView(context: Context, listener: KeyboardListener): ViewWrapper {
        val wrappedContext = ContextThemeWrapper(context, type.styleId)
        val binding = KeyboardKeyBinding.inflate(LayoutInflater.from(wrappedContext)).apply {
            val key = this@Key
            if(key.label != null) label.text = key.label
            if(key.icon != null) icon.setImageResource(key.icon)
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = key.width
            }
            root.setOnTouchListener { v, event ->
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        fun repeater() {
                            listener.onKey(key.code, key.output)
                            handler.postDelayed({ repeater() }, repeatDelay)
                        }
                        handler.postDelayed({
                            repeater()
                        }, repeatStartDelay)
                    }
                    MotionEvent.ACTION_UP -> {
                        handler.removeCallbacksAndMessages(null)
                    }
                }
                false
            }
            root.setOnClickListener {
                listener.onKey(key.code, key.output)
            }
        }
        return ViewWrapper(this, binding)
    }

    enum class Type(
        @StyleRes val styleId: Int,
    ) {
        Alphanumeric(R.style.Theme_Mboard_Keyboard_Key),
        Modifier(R.style.Theme_Mboard_Keyboard_Key_Mod),
        Return(R.style.Theme_Mboard_Keyboard_Key_Return),
    }

    data class ViewWrapper(
        val key: Key,
        val binding: KeyboardKeyBinding,
    )
}
