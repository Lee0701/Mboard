package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R

data class Key(
    val code: Int,
    val output: String?,
    val label: String? = output,
    val icon: Int? = null,
    val width: Float = 1f,
    val repeatable: Boolean = false,
    val type: Type = Type.Alphanumeric,
) {
    fun initView(context: Context, theme: Theme): ViewWrapper {
        val wrappedContext = theme.key[type]?.let { DynamicColors.wrapContextIfAvailable(context, it) } ?: context
        val binding = LayoutInflater.from(wrappedContext).inflate(R.layout.keyboard_key, null, false).apply {
            val key = this@Key
            if(key.label != null) findViewById<AppCompatTextView>(R.id.label).text = key.label
            if(key.icon != null) findViewById<AppCompatImageView>(R.id.icon).setImageResource(key.icon)
            layoutParams = LinearLayoutCompat.LayoutParams(
                0, LinearLayoutCompat.LayoutParams.MATCH_PARENT
            ).apply {
                weight = key.width
            }
        }
        return ViewWrapper(this, binding)
    }

    enum class Type {
        Alphanumeric,
        AlphanumericAlt,
        Modifier,
        ModifierAlt,
        Space,
        Return,
    }

    data class ViewWrapper(
        val key: Key,
        val view: View,
    )
}
