package io.github.lee0701.mboard.view.keyboard

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.util.TypedValue
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.view.ContextThemeWrapper
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.KeyPopupMorekeysBinding
import io.github.lee0701.mboard.databinding.KeyPopupMorekeysKeyBinding
import io.github.lee0701.mboard.databinding.KeyPopupMorekeysRowBinding
import kotlin.math.roundToInt

class MoreKeysPopup(
    context: Context,
    key: KeyboardView.KeyWrapper,
): KeyboardPopup(context, key) {
    private val wrappedContext = ContextThemeWrapper(context, R.style.Theme_MBoard_Keyboard_KeyPopup)
    private val inflater = LayoutInflater.from(wrappedContext)
    private val binding = KeyPopupMorekeysBinding.inflate(inflater, null, false)

    private val animator: Animator = ValueAnimator.ofFloat(1f, 0f).apply {
        addUpdateListener {
            val value = animatedValue as Float
            popupWindow.background.alpha = (value * 256).toInt()
            binding.root.alpha = value
        }
        addListener(object: AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                popupWindow.dismiss()
                popupWindow.background.alpha = 255
                binding.root.alpha = 1f
            }
        })
    }

    override fun show(parent: View, parentX: Int, parentY: Int) {
        popupWindow.apply {
            this.contentView = binding.root
            binding.root.removeAllViews()
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

        val x = parentX - popupWindow.width / 2
        val y = parentY - popupWindow.height / 2 * 3

        val rowView = KeyPopupMorekeysRowBinding.inflate(inflater, null, false)
        binding.root.addView(rowView.root)
        "ARST".map { c ->
            val keyView = KeyPopupMorekeysKeyBinding.inflate(inflater, null, false)
            keyView.label.text = c.toString()
            rowView.root.addView(keyView.root)
            val popupWidth = wrappedContext.resources.getDimension(R.dimen.key_popup_morekeys_width)
            val popupHeight = wrappedContext.resources.getDimension(R.dimen.key_popup_morekeys_height)
            val width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, popupWidth, context.resources.displayMetrics).roundToInt()
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, popupHeight, context.resources.displayMetrics).roundToInt()
            keyView.root.layoutParams = LinearLayoutCompat.LayoutParams(width, height)
        }
//        if(animator.isRunning) animator.cancel()

        popupWindow.showAtLocation(parent, Gravity.NO_GRAVITY, x, y)
    }

    override fun touchMove(x: Int, y: Int) {
    }

    override fun dismiss() {
        animator.start()
    }

    override fun cancel() {
        animator.cancel()
        popupWindow.dismiss()
    }
}