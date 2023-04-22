package io.github.lee0701.mboard.view.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.databinding.KeyboardBinding
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding
import io.github.lee0701.mboard.databinding.KeyboardRowBinding
import io.github.lee0701.mboard.databinding.KeyboardSpacerBinding
import io.github.lee0701.mboard.module.softkeyboard.Key
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.softkeyboard.Row
import io.github.lee0701.mboard.module.softkeyboard.Spacer
import kotlin.math.roundToInt

open class StackedViewKeyboardView(
    context: Context,
    attrs: AttributeSet?,
    keyboard: Keyboard,
    theme: Theme,
    listener: KeyboardListener,
): KeyboardView(context, attrs, keyboard, theme, listener) {

    private val keyboardViewWrapper = initKeyboardView(keyboard, theme, listener)
    override val wrappedKeys: List<KeyWrapper> = keyboardViewWrapper.keys.toList()

    init {
        this.addView(keyboardViewWrapper.binding.root)
        this.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyboardView(keyboard: Keyboard, theme: Theme, listener: KeyboardListener): KeyboardViewWrapper {
        val wrappedContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)

        val rowViewWrappers = mutableListOf<RowViewWrapper>()
        val binding = KeyboardBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            root.layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT, keyboardHeight
            )
            keyboard.rows.forEach { row ->
                val rowViewBinding = initRowView(row, theme)
                rowViewWrappers += rowViewBinding
                root.addView(rowViewBinding.binding.root)
            }
        }
        return KeyboardViewWrapper(keyboard, binding, rowViewWrappers, rowViewWrappers.flatMap { it.keys })
    }

    private fun initRowView(row: Row, theme: Theme): RowViewWrapper {
        val wrappers = mutableListOf<KeyLikeViewWrapper>()
        val binding = KeyboardRowBinding.inflate(LayoutInflater.from(context), null, false).apply {
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0
            ).apply {
                weight = 1f
            }
            row.keys.forEach { keyLike ->
                when(keyLike) {
                    is Key -> {
                        val keyViewWrapper = initKeyView(keyLike, this, theme)
                        wrappers += keyViewWrapper
                        root.addView(keyViewWrapper.binding.root)
                    }
                    is Spacer -> {
                        val spacerViewWrapper = initSpacerView(keyLike)
                        wrappers += spacerViewWrapper
                        root.addView(spacerViewWrapper.binding.root)
                    }
                }
            }
        }
        return RowViewWrapper(row, binding, wrappers)
    }

    private fun initSpacerView(spacerModel: Spacer): SpacerViewWrapper {
        val binding = KeyboardSpacerBinding.inflate(LayoutInflater.from(context), null, false).apply {
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = spacerModel.width
            }
        }
        return SpacerViewWrapper(spacerModel, binding)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyView(keyModel: Key, row: KeyboardRowBinding, theme: Theme): KeyViewWrapper {
        val wrappedContext = theme.keyBackground[keyModel.type]?.let { DynamicColors.wrapContextIfAvailable(context, it) } ?: context
        val binding = KeyboardKeyBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            val icon = theme.keyIcon[keyModel.iconType]
            if(keyModel.label != null) this.label.text = keyModel.label
            if(icon != null) this.icon.setImageResource(icon)
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = keyModel.width
            }
        }
        return KeyViewWrapper(keyModel, row, binding)
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
        val keyLikes: List<KeyLikeViewWrapper>,
    ) {
        val keys: List<KeyViewWrapper> = keyLikes.filterIsInstance<KeyViewWrapper>()
    }

    interface KeyLikeViewWrapper

    data class KeyViewWrapper(
        override val key: Key,
        private val row: KeyboardRowBinding,
        val binding: KeyboardKeyBinding,
    ): KeyLikeViewWrapper, KeyWrapper {
        override val x: Int get() = binding.root.x.roundToInt()
        override val y: Int get() = row.root.y.roundToInt()
        override val width: Int get() = binding.root.width
        override val height: Int get() = row.root.height
        override val icon: Drawable? get() = binding.icon.drawable
    }

    data class SpacerViewWrapper(
        override val spacer: Spacer,
        val binding: KeyboardSpacerBinding,
    ): KeyLikeViewWrapper, SpacerWrapper {
        override val x: Int get() = binding.root.x.roundToInt()
        override val y: Int get() = binding.root.y.roundToInt()
        override val width: Int get() = binding.root.width
        override val height: Int get() = binding.root.height
    }

    override fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        keyboardViewWrapper.keys.forEach { key ->
            val icon = icons[key.key.code]
            val label = labels[key.key.code]
            if(icon != null) key.binding.icon.setImageDrawable(icon)
            if(label != null) key.binding.label.text = label
        }
    }

    override fun updateMoreKeyKeyboards(keyboards: Map<Int, Keyboard>) {
        moreKeysKeyboards.clear()
        moreKeysKeyboards += keyboards
    }

    override fun postViewChanged() {
        wrappedKeys.filterIsInstance<KeyViewWrapper>().forEach { key ->
            key.binding.root.isPressed = keyStates[key.key.code] == true
        }
    }

    override fun highlight(key: KeyWrapper) {
        wrappedKeys.filterIsInstance<KeyViewWrapper>().forEach { it.binding.root.isPressed = false }
        val wrappedKey = wrappedKeys.filterIsInstance<KeyViewWrapper>().find { it == key } ?: return
        wrappedKey.binding.root.isPressed = true
    }

}