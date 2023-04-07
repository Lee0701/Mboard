package io.github.lee0701.mboard.keyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.PopupWindow
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.github.lee0701.mboard.R
import kotlin.math.roundToInt

class KeyPopup(
    context: Context,
) {
    private val animator: Animator = ValueAnimator.ofFloat(1f, 0f).apply {
        addUpdateListener {
            val value = animatedValue as Float
            popupWindow.background.alpha = (value * 256).toInt()
            view.alpha = value
        }
        addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                popupWindow.dismiss()
                popupWindow.background.alpha = 255
                view.alpha = 1f
            }
        })
    }

    private val wrappedContext = ContextThemeWrapper(context, R.style.Theme_MBoard_Keyboard_KeyPopup)
    private val view = LayoutInflater.from(wrappedContext).inflate(R.layout.key_popup, null, false)
    private val popupWindow = PopupWindow(wrappedContext, null).apply {
        this.contentView = view
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
        val keyIcon = key.view.findViewById<AppCompatImageView>(R.id.icon)
        val keyLabel = key.view.findViewById<AppCompatTextView>(R.id.label)
        val icon = view.findViewById<AppCompatImageView>(R.id.icon)
        val label = view.findViewById<AppCompatTextView>(R.id.label)
        if(keyIcon.drawable != null) icon.setImageDrawable(keyIcon.drawable)
        else icon.setImageDrawable(null)
        if(keyLabel.text != null) label.text = keyLabel.text
        else label.text = ""
        if(animator.isRunning) animator.cancel()
        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y)
    }

    fun hide() {
        animator.start()
    }

    fun cancel() {
        animator.cancel()
    }
}