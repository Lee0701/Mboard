package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.FrameLayout

class TouchInterceptingFrameLayout(
    context: Context,
    attrs: AttributeSet?,
): FrameLayout(context, attrs) {
    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean = true
}