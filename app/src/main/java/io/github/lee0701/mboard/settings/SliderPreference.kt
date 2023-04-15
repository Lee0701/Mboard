package io.github.lee0701.mboard.settings

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.slider.Slider
import io.github.lee0701.mboard.R

class SliderPreference(
    context: Context,
    attrs: AttributeSet?,
): Preference(context, attrs) {

    private val valueFrom: Float
    private val valueTo: Float
    private val stepSize: Float

    init {
        val a = context.obtainStyledAttributes(attrs, R.styleable.SliderPreference)
        valueFrom = a.getFloat(R.styleable.SliderPreference_valueFrom, 0f)
        valueTo = a.getFloat(R.styleable.SliderPreference_valueTo, 1f)
        stepSize = a.getFloat(R.styleable.SliderPreference_stepSize, 0f)
        a.recycle()

        layoutResource = R.layout.preference_multiline
        widgetLayoutResource = R.layout.pref_slider_widget
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)

        val slider = holder.itemView.findViewById<Slider>(R.id.slider)
        slider.valueFrom = this.valueFrom
        slider.valueTo = this.valueTo
        slider.stepSize = this.stepSize

        slider.value = getPersistedFloat(slider.valueFrom)
        slider.addOnChangeListener { _, value, _ ->
            persistFloat(value)
        }
    }

    override fun getPersistedFloat(defaultReturnValue: Float): Float {
        val v = super.getPersistedFloat(defaultReturnValue)
        if(v !in valueFrom .. valueTo) return defaultReturnValue
        return v
    }

    override fun onSetInitialValue(defaultValue: Any?) {
        super.onSetInitialValue(defaultValue)
        if(defaultValue is Float) persistFloat(defaultValue)
    }

    override fun onGetDefaultValue(a: TypedArray, index: Int): Any {
        return a.getFloat(index, 0f)
    }
}