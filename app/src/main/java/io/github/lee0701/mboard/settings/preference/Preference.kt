package io.github.lee0701.mboard.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.view.MarginLayoutParamsCompat
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import io.github.lee0701.mboard.R

class Preference(
    context: Context,
    atts: AttributeSet?,
): Preference(context, atts) {
    init {
        layoutResource = R.layout.preference_inline
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        val icon = holder.findViewById(android.R.id.icon) as AppCompatImageView
        val layoutParams = icon.layoutParams as LinearLayoutCompat.LayoutParams
        val margin = if(icon.drawable != null)
            context.resources.getDimensionPixelSize(R.dimen.pref_icon_margin)
        else 0
        MarginLayoutParamsCompat.setMarginEnd(layoutParams, margin)
    }
}