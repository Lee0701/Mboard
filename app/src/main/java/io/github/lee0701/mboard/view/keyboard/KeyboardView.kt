package io.github.lee0701.mboard.view.keyboard

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.media.AudioManager
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.KeyEvent
import android.view.MotionEvent
import android.widget.FrameLayout
import androidx.preference.PreferenceManager
import io.github.lee0701.mboard.module.softkeyboard.Key
import io.github.lee0701.mboard.module.softkeyboard.KeyType
import io.github.lee0701.mboard.module.softkeyboard.Keyboard
import io.github.lee0701.mboard.module.softkeyboard.Spacer
import kotlin.math.abs
import kotlin.math.roundToInt

abstract class KeyboardView(
    context: Context,
    attrs: AttributeSet?,
    protected val keyboard: Keyboard,
    protected val theme: Theme,
    protected val listener: KeyboardListener,
): FrameLayout(context, attrs) {
    protected val preferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

    protected val unifyHeight: Boolean = preferences.getBoolean("appearance_unify_height", false)
    protected val keyboardWidth = context.resources.displayMetrics.widthPixels.toFloat()
    protected val rowHeight: Int = dipToPixel(preferences.getFloat("appearance_keyboard_height", 55f)).toInt()
    protected val keyboardHeight: Int = if(unifyHeight) rowHeight * 4 else rowHeight * keyboard.rows.size

    protected val typedValue = TypedValue()

    protected val showKeyPopups = preferences.getBoolean("behaviour_show_popups", true)
    protected val longPressDuration = preferences.getFloat("behaviour_long_press_duration", 100f).toLong()
    protected val repeatOnLongPress = preferences.getString("behaviour_long_press_action", "shift") == "repeat"
    protected val repeatInterval = preferences.getFloat("behaviour_repeat_interval", 50f).toLong()

    protected val slideAction = preferences.getString("behaviour_slide_action", "flick")
    protected val flickSensitivity = dipToPixel(preferences.getFloat("behaviour_flick_sensitivity", 100f)).toInt()

    protected val hapticFeedback = preferences.getBoolean("appearance_haptic_feedback", true)
    protected val soundFeedback = preferences.getBoolean("appearance_sound_feedback", true)

    protected val pointers: MutableMap<Int, TouchPointer> = mutableMapOf()
    protected val keyStates: MutableMap<Int, Boolean> = mutableMapOf()
    private var keyPopups: MutableMap<Int, KeyPopup> = mutableMapOf()

    protected abstract val wrappedKeys: List<KeyLikeWrapper>

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) return super.onTouchEvent(event)
        val pointerId = event.getPointerId(event.actionIndex)
        val x = event.getX(event.actionIndex).roundToInt()
        val y = event.getY(event.actionIndex).roundToInt()

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val key = findKey(x, y) ?: return true
                onTouchDown(key, pointerId, x, y)
                postViewChanged()
            }
            MotionEvent.ACTION_MOVE -> {
                val key = pointers[pointerId]?.key ?: findKey(x, y) ?: return true
                onTouchMove(key, pointerId, x, y)
                postViewChanged()
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                val key = pointers[pointerId]?.key ?: findKey(x, y) ?: return true
                onTouchUp(key, pointerId, x, y)
                postViewChanged()
            }
        }
        return true
    }

    protected fun onTouchDown(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        if(this.hapticFeedback) this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        if(this.soundFeedback) this.performSoundFeedback(key.key.code)
        maybeShowPopup(key, pointerId)
        fun repeater() {
            listener.onKeyClick(key.key.code, key.key.output)
            handler.postDelayed({ repeater() }, repeatInterval)
        }
        handler.postDelayed({
            if(key.key.repeatable || repeatOnLongPress) {
                repeater()
            } else {
                if(this.hapticFeedback) this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                listener.onKeyLongClick(key.key.code, key.key.output)
            }
        }, longPressDuration)

        listener.onKeyDown(key.key.code, key.key.output)
        keyStates[key.key.code] = true
        val pointer = TouchPointer(x, y, key)
        pointers += pointerId to pointer
    }

    protected fun onTouchMove(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        val pointer = pointers[pointerId] ?: return
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
            onFlick(direction, pointer.key, pointerId, x, y)
            pointers[pointerId] = pointer.copy(flickDirection = direction)

        } else if(slideAction == "seek" && key.key.code !in setOf(KeyEvent.KEYCODE_DEL)) {
            handler.removeCallbacksAndMessages(null)

            if(x !in key.x until key.x+key.width
                || y !in key.y until key.y+key.height) {

                val newKey = findKey(x, y) ?: key
                if(newKey.key.code != key.key.code) {
                    keyStates[key.key.code] = false
                    keyStates[newKey.key.code] = true
                    pointers[pointerId] = pointer.copy(key = newKey)
                    keyPopups[pointerId]?.cancel()
                    maybeShowPopup(newKey, pointerId)
                }
            }
        }
    }

    protected fun onTouchUp(key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        handler.removeCallbacksAndMessages(null)
        keyPopups[pointerId]?.hide()
        keyStates[key.key.code] = false
        listener.onKeyClick(key.key.code, key.key.output)
        listener.onKeyUp(key.key.code, key.key.output)
        performClick()
        pointers -= pointerId
    }

    protected fun onFlick(flickDirection: FlickDirection, key: KeyWrapper, pointerId: Int, x: Int, y: Int) {
        listener.onKeyFlick(flickDirection, key.key.code, key.key.output)
    }

    abstract fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>)
    protected abstract fun findKey(x: Int, y: Int): KeyWrapper?
    protected abstract fun showPopup(key: KeyWrapper, popup: KeyPopup)
    protected abstract fun postViewChanged()

    private fun performSoundFeedback(keyCode: Int) {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val fx = when(keyCode) {
            KeyEvent.KEYCODE_DEL -> AudioManager.FX_KEYPRESS_DELETE
            KeyEvent.KEYCODE_ENTER -> AudioManager.FX_KEYPRESS_RETURN
            KeyEvent.KEYCODE_SPACE -> AudioManager.FX_KEYPRESS_SPACEBAR
            else -> AudioManager.FX_KEYPRESS_STANDARD
        }
        audioManager.playSoundEffect(fx, 1f)
    }

    private fun dipToPixel(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics)
    }

    private fun maybeShowPopup(key: KeyWrapper, pointerId: Int) {
        if(showKeyPopups &&
            (key.key.type == KeyType.Alphanumeric || key.key.type == KeyType.AlphanumericAlt)) {
            val keyPopup = keyPopups.getOrPut(pointerId) { KeyPopup(context) }
            showPopup(key, keyPopup)
        } else {
            keyPopups[pointerId]?.cancel()
        }
    }

    interface KeyLikeWrapper {
        val x: Int
        val y: Int
        val width: Int
        val height: Int
    }

    interface KeyWrapper: KeyLikeWrapper {
        val key: Key
        val icon: Drawable?
    }

    interface SpacerWrapper: KeyLikeWrapper {
        val spacer: Spacer
    }

    data class TouchPointer(
        val initialX: Int,
        val initialY: Int,
        val key: KeyWrapper,
        val flickDirection: FlickDirection = FlickDirection.None,
    )

}