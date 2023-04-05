package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.FrameLayout
import io.github.lee0701.mboard.databinding.KeyboardBinding

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {

    fun initView(context: Context, listener: KeyboardListener): ViewWrapper {
        val rowViewWrappers = mutableListOf<Row.ViewWrapper>()
        val view = KeyboardBinding.inflate(LayoutInflater.from(context)).apply {
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@Keyboard.height, context.resources.displayMetrics).toInt()
            root.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height
            )
            rows.forEach { row ->
                val rowViewWrapper = row.initView(context, listener)
                rowViewWrappers += rowViewWrapper
                root.addView(rowViewWrapper.binding.root)
            }
        }
        val keyViewWrappers = rowViewWrappers.flatMap { it.keys }
        return ViewWrapper(this, view, rowViewWrappers, keyViewWrappers)
    }

    data class ViewWrapper(
        val keyboard: Keyboard,
        val binding: KeyboardBinding,
        val rows: List<Row.ViewWrapper>,
        val keys: List<Key.ViewWrapper>,
    ) {
        val keyMap = keys.associateBy { it.key.code }
    }
}
