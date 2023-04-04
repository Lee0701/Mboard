package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.lee0701.mboard.KeyboardListener
import io.github.lee0701.mboard.databinding.KeyboardRowBinding

data class Row(
    val keys: List<Key>,
    val padding: Float = 0f,
) {
    fun initView(context: Context, listener: KeyboardListener): View {
        return KeyboardRowBinding.inflate(LayoutInflater.from(context)).root.apply {
            layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0
            ).apply {
                weight = 1f
            }
            if(padding > 0) addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = padding
                }
            })
            keys.forEach { key ->
                addView(key.initView(context, listener))
            }
            if(padding > 0) addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = padding
                }
            })
        }
    }
}
