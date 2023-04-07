package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.LinearLayoutCompat
import io.github.lee0701.mboard.databinding.KeyboardRowBinding

data class Row(
    val keys: List<Key>,
    val padding: Float = 0f,
) {
    fun initView(context: Context, theme: Theme): ViewWrapper {
        val keyViewWrappers = mutableListOf<Key.ViewWrapper>()
        val view = KeyboardRowBinding.inflate(LayoutInflater.from(context), null, false).apply {
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                LinearLayoutCompat.LayoutParams.MATCH_PARENT, 0
            ).apply {
                weight = 1f
            }
            if(padding > 0) root.addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = padding
                }
            })
            keys.forEach { key ->
                val keyViewWrapper = key.initView(context, theme)
                keyViewWrappers += keyViewWrapper
                root.addView(keyViewWrapper.binding.root)
            }
            if(padding > 0) root.addView(View(context, null).apply {
                layoutParams = LinearLayoutCompat.LayoutParams(
                    0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
                ).apply {
                    weight = padding
                }
            })
        }
        return ViewWrapper(this, view, keyViewWrappers)
    }

    data class ViewWrapper(
        val row: Row,
        val binding: KeyboardRowBinding,
        val keys: List<Key.ViewWrapper>,
    )
}
