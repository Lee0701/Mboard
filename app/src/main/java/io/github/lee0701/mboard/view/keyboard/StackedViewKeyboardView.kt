package io.github.lee0701.mboard.view.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.databinding.KeyboardBinding
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding
import io.github.lee0701.mboard.databinding.KeyboardRowBinding
import io.github.lee0701.mboard.module.KeyType
import io.github.lee0701.mboard.module.Keyboard
import io.github.lee0701.mboard.module.Row
import io.github.lee0701.mboard.module.Key
import kotlin.math.roundToInt

class StackedViewKeyboardView(
    context: Context,
    attrs: AttributeSet?,
    keyboard: Keyboard,
    theme: Theme,
    listener: KeyboardListener,
): KeyboardView(context, attrs, keyboard, theme, listener) {

    private val keyboardViewWrapper = initKeyboardView(keyboard, theme, listener)
    private var keyPopup: KeyPopup? = null

    init {
        this.addView(keyboardViewWrapper.binding.root)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyboardView(keyboard: Keyboard, theme: Theme, listener: KeyboardListener): KeyboardViewWrapper {
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

        val longPressDuration = sharedPreferences.getInt("behaviour_long_press_duration", 1000).toLong()
        val repeatInterval = sharedPreferences.getInt("behaviour_repeat_interval", 50).toLong()
        val showKeyPopups = sharedPreferences.getBoolean("behaviour_show_popups", true)

        val wrappedContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)

        val rowViewWrappers = mutableListOf<RowViewWrapper>()
        val binding = KeyboardBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            keyboard.rows.forEach { row ->
                val rowViewBinding = initRowView(row, theme)
                rowViewWrappers += rowViewBinding
                root.addView(rowViewBinding.binding.root)
            }
        }

        keyPopup = KeyPopup(context)

        val keyViewWrappers = rowViewWrappers.flatMap { it.keys }
        keyViewWrappers.forEach { key ->
            key.binding.root.setOnTouchListener { v, event ->
                when(event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> {
                        v.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                        if(showKeyPopups &&
                            (key.key.type == KeyType.Alphanumeric || key.key.type == KeyType.AlphanumericAlt)) {
                            keyPopup?.apply {
                                val row = rowViewWrappers.find { key in it.keys } ?: return@apply
                                val x = key.binding.root.x.roundToInt() + key.binding.root.width / 2
                                val y = row.binding.root.y.roundToInt() + row.binding.root.height / 2
                                show(binding.root, key.key.label, key.binding.icon.drawable, x, y)
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
            key.binding.root.setOnClickListener {
                listener.onKeyClick(key.key.code, key.key.output)
            }
        }
        return KeyboardViewWrapper(keyboard, binding, rowViewWrappers, rowViewWrappers.flatMap { it.keys })
    }


    private fun initRowView(row: Row, theme: Theme): RowViewWrapper {
        val keyViewWrappers = mutableListOf<KeyViewWrapper>()
        val binding = KeyboardRowBinding.inflate(LayoutInflater.from(context), null, false).apply {
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0
            ).apply {
                weight = 1f
            }
            if(row.padding > 0) root.addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = row.padding
                }
            })
            row.keys.forEach { key ->
                val keyViewWrapper = initKeyView(key, theme)
                keyViewWrappers += keyViewWrapper
                root.addView(keyViewWrapper.binding.root)
            }
            if(row.padding > 0) root.addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = row.padding
                }
            })
        }
        return RowViewWrapper(row, binding, keyViewWrappers)
    }

    private fun initKeyView(key: Key, theme: Theme): KeyViewWrapper {
        val wrappedContext = theme.keyBackground[key.type]?.let { DynamicColors.wrapContextIfAvailable(context, it) } ?: context
        val binding = KeyboardKeyBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            val icon = theme.keyIcon[key.iconType]
            if(key.label != null) this.label.text = key.label
            if(icon != null) this.icon.setImageResource(icon)
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = key.width
            }
        }
        return KeyViewWrapper(key, binding)
    }

    data class KeyboardViewWrapper(
        val keyboard: Keyboard,
        val binding: KeyboardBinding,
        val rows: List<RowViewWrapper>,
        val keys: List<KeyViewWrapper>,
    )

    data class RowViewWrapper(
        val row: Row,
        val binding: KeyboardRowBinding,
        val keys: List<KeyViewWrapper>,
    )

    data class KeyViewWrapper(
        val key: Key,
        val binding: KeyboardKeyBinding,
    )

    override fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        keyboardViewWrapper.keys.forEach { key ->
            val icon = icons[key.key.code]
            val label = labels[key.key.code]
            if(icon != null) key.binding.icon.setImageDrawable(icon)
            if(label != null) key.binding.label.text = label
        }
    }
}