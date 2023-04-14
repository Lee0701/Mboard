package io.github.lee0701.mboard.view.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.KeyboardBinding
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding
import io.github.lee0701.mboard.databinding.KeyboardRowBinding
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.softkeyboard.Row
import io.github.lee0701.mboard.module.softkeyboard.Key
import kotlin.math.abs
import kotlin.math.roundToInt

class StackedViewKeyboardView(
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
    }

    override fun showPopup(key: KeyWrapper, popup: KeyPopup) {
        if(key is KeyViewWrapper) popup.apply {
            val parentX = key.x + key.width/2
            val row = keyboardViewWrapper.rows.find { key in it.keys } ?: return
            val parentY = row.binding.root.y + resources.getDimension(R.dimen.candidates_view_height).toInt() + row.binding.root.height/2
            show(this@StackedViewKeyboardView, key.binding.label.text, key.icon, parentX, parentY.roundToInt())
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initKeyboardView(keyboard: Keyboard, theme: Theme, listener: KeyboardListener): KeyboardViewWrapper {
        val wrappedContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)

        val rowViewWrappers = mutableListOf<RowViewWrapper>()
        val binding = KeyboardBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            keyboard.rows.forEach { row ->
                val rowViewBinding = initRowView(row, theme)
                rowViewWrappers += rowViewBinding
                root.addView(rowViewBinding.binding.root)
            }
        }

        return KeyboardViewWrapper(keyboard, binding, rowViewWrappers, rowViewWrappers.flatMap { it.keys })
    }

    private fun initRowView(row: Row, theme: Theme): RowViewWrapper {
        val keyViewWrappers = mutableListOf<KeyViewWrapper>()
        val binding = KeyboardRowBinding.inflate(LayoutInflater.from(context), null, false).apply {
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT, rowHeight.roundToInt()
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

    @SuppressLint("ClickableViewAccessibility")
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
        val keyViewWrapper = KeyViewWrapper(key, binding)
        binding.root.setOnTouchListener { _, event ->
            if(event == null) return@setOnTouchListener false
            val pointerId = event.getPointerId(event.actionIndex)
            val x = event.getX(event.actionIndex).roundToInt()
            val y = event.getY(event.actionIndex).roundToInt()

            when(event.actionMasked) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                    onTouchDown(keyViewWrapper, pointerId, x, y)
                    binding.root.isPressed = true
                    postViewChanged()
                }
                MotionEvent.ACTION_MOVE -> {
                    val pointer = pointers[pointerId] ?: return@setOnTouchListener true
                    val dx = abs(pointer.initialX - x)
                    val dy = abs(pointer.initialY - y)
                    val direction = if(dx > flickSensitivity && dx > dy) {
                        if(x < pointer.initialX) FlickDirection.Left
                        else FlickDirection.Right
                    } else if(dy > flickSensitivity && dy > dx) {
                        if(y < pointer.initialY) FlickDirection.Up
                        else FlickDirection.Down
                    } else FlickDirection.None
                    if(slideAction == "flick"
                        && direction != FlickDirection.None
                        && pointer.flickDirection == FlickDirection.None) {
                        handler.removeCallbacksAndMessages(null)
                        listener.onKeyFlick(direction, pointer.key.key.code, pointer.key.key.output)
                        pointers[pointerId] = pointer.copy(flickDirection = direction)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                    onTouchUp(keyViewWrapper, pointerId, x, y)
                    binding.root.isPressed = false
                    postViewChanged()
                }
            }
            true
        }
        return keyViewWrapper
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
        override val key: Key,
        val binding: KeyboardKeyBinding,
    ): KeyWrapper {
        override val x: Int get() = binding.root.x.roundToInt()
        override val y: Int get() = binding.root.y.roundToInt()
        override val width: Int get() = binding.root.width
        override val height: Int get() = binding.root.height
        override val icon: Drawable? get() = binding.icon.drawable
    }

    override fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        keyboardViewWrapper.keys.forEach { key ->
            val icon = icons[key.key.code]
            val label = labels[key.key.code]
            if(icon != null) key.binding.icon.setImageDrawable(icon)
            if(label != null) key.binding.label.text = label
        }
    }

    override fun findKey(x: Int, y: Int): KeyWrapper? {
        keyboardViewWrapper.rows.forEach { row ->
            val rowY = row.binding.root.y.toInt()
            val rowHeight = row.binding.root.height
            if(y in rowY until rowY+rowHeight) {
                row.keys.forEach { key ->
                    val keyX = key.binding.root.x.toInt()
                    val keyWidth = key.binding.root.height
                    if(x in keyX until keyX+keyWidth) {
                        return key
                    }
                }
            }
        }
        return null
    }

    override fun postViewChanged() {
    }
}