package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.widget.FrameLayout
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
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

    protected val keyboardBackground: Drawable
    protected val keyBackgrounds: Map<KeyType, Pair<Drawable, ColorStateList>>
    protected val keyIconTints: Map<KeyType, Int>
    protected val keyLabelTextColors: Map<KeyType, Int>
    protected val keyLabelTextSizes: Map<KeyType, Float>

    protected val typedValue = TypedValue()

    protected val keyMarginHorizontal: Float
    protected val keyMarginVertical: Float

    protected val showKeyPopups = sharedPreferences.getBoolean("behaviour_show_popups", true)
    protected val longPressDuration = sharedPreferences.getInt("behaviour_long_press_duration", 500).toLong()
    protected val repeatInterval = sharedPreferences.getInt("behaviour_repeat_interval", 50).toLong()

    init {
        val keyboardContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground).let {
            if(it == context) ContextThemeWrapper(context, theme.keyboardBackground) else it
        }
        keyboardContext.theme.resolveAttribute(R.attr.background, typedValue, true)
        val background = ContextCompat.getDrawable(keyboardContext, typedValue.resourceId) ?: ColorDrawable(
            Color.WHITE)
        keyboardContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
        val backgroundTint = ContextCompat.getColor(keyboardContext, typedValue.resourceId)
        DrawableCompat.setTint(background, backgroundTint)
        this.keyboardBackground = background

        val keyContexts = theme.keyBackground.mapValues { (_, id) ->
            DynamicColors.wrapContextIfAvailable(context, id).let {
                if(it == context) ContextThemeWrapper(context, id) else it
            }
        }
        keyBackgrounds = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(R.attr.background, typedValue, true)
            val keyBackground = ContextCompat.getDrawable(keyContext, typedValue.resourceId) ?: ColorDrawable(
                Color.TRANSPARENT)
            keyContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
            val keyBackgroundTint = ContextCompat.getColorStateList(keyContext, typedValue.resourceId) ?: ColorStateList(arrayOf(), intArrayOf())
            keyBackground to keyBackgroundTint
        }
        keyIconTints = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(R.attr.iconTint, typedValue, true)
            ContextCompat.getColor(keyContext, typedValue.resourceId)
        }
        keyLabelTextColors = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
            ContextCompat.getColor(keyContext, typedValue.resourceId)
        }
        keyLabelTextSizes = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(android.R.attr.textSize, typedValue, true)
            context.resources.getDimension(typedValue.resourceId)
        }
        keyMarginHorizontal = resources.getDimension(R.dimen.key_margin_horizontal)
        keyMarginVertical = resources.getDimension(R.dimen.key_margin_vertical)
    }

    abstract fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>)

    protected fun dipToPixel(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics)
    }

}