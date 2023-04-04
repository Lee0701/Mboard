package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import io.github.lee0701.mboard.KeyboardListener
import io.github.lee0701.mboard.databinding.KeyboardBinding

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {

    fun initView(context: Context, listener: KeyboardListener): View {
        return KeyboardBinding.inflate(LayoutInflater.from(context)).root.apply {
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@Keyboard.height, context.resources.displayMetrics).toInt()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height
            )
            rows.forEach { row ->
                addView(row.initView(context, listener))
            }
        }
    }

}
