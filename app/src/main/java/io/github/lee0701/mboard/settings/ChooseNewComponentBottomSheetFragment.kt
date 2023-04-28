package io.github.lee0701.mboard.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.github.lee0701.mboard.databinding.BottomsheetChooseNewComponentBinding
import io.github.lee0701.mboard.databinding.ListitemBottomsheetChooseNewComponentBinding

class ChooseNewComponentBottomSheetFragment(
    private val types: List<KeyboardLayoutSettingsFragment.ComponentType> = listOf(),
    private val onItemClicked: (KeyboardLayoutSettingsFragment.ComponentType) -> Unit,
): BottomSheetDialogFragment() {

    private var binding: BottomsheetChooseNewComponentBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = BottomsheetChooseNewComponentBinding.inflate(inflater, container, false)
        val adapter = Adapter()
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            this.adapter = adapter
        }
        adapter.submitList(types)
        this.binding = binding
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    inner class Adapter: ListAdapter<KeyboardLayoutSettingsFragment.ComponentType, ViewHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val inflater = LayoutInflater.from(parent.context)
            val binding = ListitemBottomsheetChooseNewComponentBinding.inflate(inflater, parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.onBind(getItem(position))
        }

    }

    inner class ViewHolder(
        private val binding: ListitemBottomsheetChooseNewComponentBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(componentType: KeyboardLayoutSettingsFragment.ComponentType) {
            binding.icon.setImageResource(componentType.iconRes)
            binding.title.setText(componentType.titleRes)
            binding.root.setOnClickListener {
                onItemClicked(componentType)
                dismiss()
            }
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<KeyboardLayoutSettingsFragment.ComponentType>() {
        override fun areItemsTheSame(
            oldItem: KeyboardLayoutSettingsFragment.ComponentType,
            newItem: KeyboardLayoutSettingsFragment.ComponentType
        ): Boolean = oldItem === newItem

        override fun areContentsTheSame(
            oldItem: KeyboardLayoutSettingsFragment.ComponentType,
            newItem: KeyboardLayoutSettingsFragment.ComponentType
        ): Boolean = oldItem == newItem
    }

    companion object {
        const val TAG = "ChooseNewComponentBottomSheet"
    }
}