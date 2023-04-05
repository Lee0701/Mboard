package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.KeyPopupBinding
import kotlin.math.roundToInt

class KeyPopup(
    context: Context,
) {
    private val wrappedContext = ContextThemeWrapper(context, R.style.Theme_Mboard_Keyboard_KeyPopup)
    private val binding = KeyPopupBinding.inflate(LayoutInflater.from(wrappedContext), null, false)
    private val popupWindow = PopupWindow(wrappedContext, null).apply {
        this.contentView = binding.root
        val popupWidth = wrappedContext.resources.getDimension(R.dimen.key_popup_width)
        val popupHeight = wrappedContext.resources.getDimension(R.dimen.key_popup_height)
        this.width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, popupWidth, context.resources.displayMetrics).roundToInt()
        this.height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, popupHeight, context.resources.displayMetrics).roundToInt()
        this.isClippingEnabled = true
        this.isTouchable = false
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.elevation = wrappedContext.resources.getDimension(R.dimen.key_popup_elevation)
        }
        val drawable = ContextCompat.getDrawable(wrappedContext, R.drawable.key_popup_bg)
        if(drawable != null) {
            val typedValue = TypedValue()
            wrappedContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
            val backgroundTint = ContextCompat.getColor(wrappedContext, typedValue.resourceId)
            val backgroundDrawable = DrawableCompat.wrap(drawable)
            DrawableCompat.setTint(backgroundDrawable.mutate(), backgroundTint)
            this.setBackgroundDrawable(backgroundDrawable)
        }
    }

    fun show(parent: View, key: Key.ViewWrapper, parentX: Int, parentY: Int) {
        val x = parentX - popupWindow.width / 2
        val y = parentY - popupWindow.height / 2 * 3
        if(key.key.icon != null) binding.icon.setImageResource(key.key.icon)
        else binding.icon.setImageDrawable(null)
        if(key.key.label != null) binding.label.text = key.key.label
        else binding.label.text = ""
        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y)
    }

    fun hide() {
        popupWindow.dismiss()
    }
}