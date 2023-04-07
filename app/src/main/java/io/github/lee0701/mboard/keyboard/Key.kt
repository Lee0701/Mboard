package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.view.LayoutInflater
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.databinding.KeyboardKeyBinding
import io.github.lee0701.mboard.module.KeyType

data class Key(
    val code: Int,
    val output: String?,
    val label: String? = output,
    val icon: Int? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: KeyType = KeyType.Alphanumeric,
) {
    fun initView(context: Context, theme: Theme): ViewWrapper {
        val wrappedContext = theme.key[type]?.let { DynamicColors.wrapContextIfAvailable(context, it) } ?: context
        val binding = KeyboardKeyBinding.inflate(LayoutInflater.from(wrappedContext), null, false).apply {
            val key = this@Key
            if(key.label != null) label.text = key.label
            if(key.icon != null) icon.setImageResource(key.icon)
            root.layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = key.width
            }
        }
        return ViewWrapper(this, binding)
    }

    data class ViewWrapper(
        val key: Key,
        val binding: KeyboardKeyBinding,
    )
}
