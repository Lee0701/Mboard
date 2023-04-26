package io.github.lee0701.mboard.settings

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.lee0701.mboard.databinding.ListitemKeyboardLayoutPreviewRowBinding
import io.github.lee0701.mboard.input.BasicSoftInputEngine
import io.github.lee0701.mboard.input.SoftInputEngine
import io.github.lee0701.mboard.module.InputEnginePreset
import kotlin.math.roundToInt

class KeyboardLayoutPreviewAdapter(
    val context: Context,
): ListAdapter<InputEnginePreset, KeyboardLayoutPreviewAdapter.ViewHolder>(DiffCallback()) {

    private var screenMode: String = "mobile"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val rootPreferences = PreferenceManager.getDefaultSharedPreferences(context)
        val screenMode = rootPreferences.getString("layout_screen_mode", "mobile") ?: "mobile"
        this.screenMode = screenMode
        return ViewHolder(ListitemKeyboardLayoutPreviewRowBinding
            .inflate(LayoutInflater.from(context), null, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.onBind(position, getItem(position))
    }

    inner class ViewHolder(
        private val binding: ListitemKeyboardLayoutPreviewRowBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(index: Int, preset: InputEnginePreset) {
            val context = binding.root.context
            val engine = mod(preset).inflate(context,
                KeyboardLayoutSettingsActivity.emptyInputEngineListener
            )
            val view = if(engine is SoftInputEngine) engine.initView(context) else null
            binding.root.removeAllViews()
            if(engine is BasicSoftInputEngine) binding.root.addView(view)
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<InputEnginePreset>() {
        override fun areItemsTheSame(
            oldItem: InputEnginePreset,
            newItem: InputEnginePreset
        ): Boolean {
            return oldItem === newItem
        }

        override fun areContentsTheSame(
            oldItem: InputEnginePreset,
            newItem: InputEnginePreset
        ): Boolean {
            return oldItem == newItem
        }
    }

    private fun mod(preset: InputEnginePreset): InputEnginePreset {
        return preset.copy(
            layout = modLayout(preset.layout),
            size = InputEnginePreset.Size(rowHeight = modHeight(preset.size.rowHeight)),
        )
    }

    private fun modLayout(layout: InputEnginePreset.Layout): InputEnginePreset.Layout {
        return layout.copy(
            softKeyboard = modFilenames(layout.softKeyboard),
            moreKeysTable = modFilenames(layout.moreKeysTable),
            codeConvertTable = modFilenames(layout.codeConvertTable),
            combinationTable = modFilenames(layout.combinationTable),
        )
    }

    private fun modFilenames(fileNames: List<String>): List<String> {
        return fileNames.map { it.format(screenMode) }
    }

    private fun modHeight(height: Int): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            height.toFloat(),
            context.resources.displayMetrics
        ).roundToInt()
    }

}