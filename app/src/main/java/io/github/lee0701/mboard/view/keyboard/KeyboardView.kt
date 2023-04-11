package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.module.Key
import io.github.lee0701.mboard.module.KeyType
import io.github.lee0701.mboard.module.Keyboard

abstract class KeyboardView(
    context: Context,
    attrs: AttributeSet?,
    protected val keyboard: Keyboard,
    protected val theme: Theme,
    protected val listener: KeyboardListener,
): FrameLayout(context, attrs) {
    protected val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    protected val unifyHeight: Boolean = sharedPreferences.getBoolean("appearance_unify_height", false)
    protected val keyboardWidth = context.resources.displayMetrics.widthPixels.toFloat()
    protected val rowHeight = dipToPixel(sharedPreferences.getInt("appearance_keyboard_height", 55).toFloat())
    protected val keyboardHeight = if(unifyHeight) rowHeight * 4 else rowHeight * keyboard.rows.size

    protected val typedValue = TypedValue()

    protected val showKeyPopups = sharedPreferences.getBoolean("behaviour_show_popups", true)
    protected val longPressDuration = sharedPreferences.getInt("behaviour_long_press_duration", 500).toLong()
    protected val repeatInterval = sharedPreferences.getInt("behaviour_repeat_interval", 50).toLong()

    protected val pointers: MutableMap<Int, TouchPointer> = mutableMapOf()
    protected val keyStates: MutableMap<Int, Boolean> = mutableMapOf()
    private var keyPopups: MutableMap<Int, KeyPopup> = mutableMapOf()

    protected abstract val wrappedKeys: List<KeyWrapper>

    protected fun onTouchDown(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        if(showKeyPopups &&
            (key.key.type == KeyType.Alphanumeric || key.key.type == KeyType.AlphanumericAlt)) {
            val keyPopup = keyPopups.getOrPut(pointerId) { KeyPopup(context) }
            showPopup(key, keyPopup)
        } else {
            keyPopups[pointerId]?.cancel()
        }
        fun repeater() {
            listener.onKeyClick(key.key.code, key.key.output)
            handler.postDelayed({ repeater() }, repeatInterval)
        }
        handler.postDelayed({
            if(key.key.repeatable) {
                repeater()
            } else {
                this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                listener.onKeyLongClick(key.key.code, key.key.output)
            }
        }, longPressDuration)

        listener.onKeyDown(key.key.code, key.key.output)
        keyStates[key.key.code] = true
        val pointer = TouchPointer(x, y, key)
        pointers += pointerId to pointer
    }

    protected fun onTouchMove(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {

    }

    protected fun onTouchUp(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        handler.removeCallbacksAndMessages(null)
        keyPopups[pointerId]?.hide()
        keyStates[key.key.code] = false
        listener.onKeyUp(key.key.code, key.key.output)
        listener.onKeyClick(key.key.code, key.key.output)
        performClick()
        pointers -= pointerId
    }

    abstract fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>)
    protected abstract fun findKey(x: Int, y: Int): KeyWrapper?
    protected abstract fun showPopup(key: KeyWrapper, popup: KeyPopup)
    protected abstract fun postViewChanged()

    private fun dipToPixel(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics)
    }

    interface KeyWrapper {
        val key: Key
        val x: Int
        val y: Int
        val width: Int
        val height: Int
        val icon: Drawable?
    }

    data class TouchPointer(
        val initialX: Int,
        val initialY: Int,
        val key: KeyWrapper,
    )

}