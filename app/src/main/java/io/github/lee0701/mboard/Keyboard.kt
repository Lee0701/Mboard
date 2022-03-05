package io.github.lee0701.mboard

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.lee0701.mboard.databinding.KeyboardBinding
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding
import io.github.lee0701.mboard.databinding.KeyboardRowBinding

data class Keyboard(
    val rows: List<Row>,
    val height: Float,
) {

    data class Row(
        val keys: List<Key>,
        val padding: Float = 0f,
    )

    data class Key(
        val code: Int,
        val output: String?,
        val label: String? = output,
        val icon: Int? = null,
        val width: Float = 1f,
    )

    fun initView(context: Context, listener: KeyboardListener): View {
        val inflater = LayoutInflater.from(context)
        return KeyboardBinding.inflate(inflater).root.apply {
            val height = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, this@Keyboard.height, context.resources.displayMetrics).toInt()
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, height
            )
            rows.forEach { row ->
                addView(KeyboardRowBinding.inflate(inflater).root.apply {
                    layoutParams = LinearLayoutCompat.LayoutParams(
                        LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0
                    ).apply {
                        weight = 1f
                    }
                    if(row.padding > 0) addView(View(context, null).apply {
                        layoutParams = LinearLayoutCompat.LayoutParams(
                            0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                        ).apply {
                            weight = row.padding
                        }
                    })
                    row.keys.forEach { key ->
                        addView(KeyboardKeyBinding.inflate(inflater).apply {
                            if(key.label != null) label.text = key.label
                            if(key.icon != null) icon.setImageResource(key.icon)
                            root.layoutParams = LinearLayoutCompat.LayoutParams(
                                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                            ).apply {
                                weight = key.width
                            }
                            root.setOnClickListener {
                                listener.onKey(key.code, key.output)
                            }
                        }.root)
                    }
                    if(row.padding > 0) addView(View(context, null).apply {
                        layoutParams = LinearLayoutCompat.LayoutParams(
                            0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                        ).apply {
                            weight = row.padding
                        }
                    })
                })
            }
        }
    }

}
