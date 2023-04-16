package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.view.View
import android.widget.PopupWindow

abstract class KeyboardPopup(
    val context: Context,
    val key: KeyboardView.KeyWrapper,
) {
    protected val popupWindow: PopupWindow = PopupWindow(context, null)

    abstract fun show(parent: View, parentX: Int, parentY: Int)
    abstract fun touchMove(x: Int, y: Int)
    abstract fun dismiss()
    abstract fun cancel()

}