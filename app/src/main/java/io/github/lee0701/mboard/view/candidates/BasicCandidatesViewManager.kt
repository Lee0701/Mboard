package io.github.lee0701.mboard.view.candidates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.github.lee0701.mboard.databinding.CandidatesViewBinding
import io.github.lee0701.mboard.databinding.CandidatesItemBinding
import io.github.lee0701.mboard.input.Candidate

class BasicCandidatesViewManager(
    val listener: Listener
) {

    private var binding: CandidatesViewBinding? = null
    private var adapter: Adapter? = null

    fun initView(context: Context): View {
        val binding = CandidatesViewBinding.inflate(LayoutInflater.from(context), null, false)
        val adapter = Adapter(context) { listener.onItemClicked(it) }
        binding.recyclerView.adapter = adapter
        this.binding = binding
        this.adapter = adapter
        return binding.root
    }

    fun showCandidates(candidates: List<Candidate>) {
        val adapter = this.adapter ?: return
        val binding = this.binding ?: return
        adapter.submitList(candidates) {
            binding.recyclerView.scrollToPosition(0)
        }
    }

    class Adapter(
        private val context: Context,
        private val onClick: (Candidate) -> Unit,
    ): ListAdapter<Candidate, ViewHolder>(DiffCallback()) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(CandidatesItemBinding.inflate(LayoutInflater.from(context)))
        }
        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val item = getItem(position)
            holder.onBind(item)
            holder.binding.root.setOnClickListener {
                onClick(item)
            }
        }
    }

    class ViewHolder(
        val binding: CandidatesItemBinding,
    ): RecyclerView.ViewHolder(binding.root) {
        fun onBind(candidate: Candidate) {
            this.binding.text.text = candidate.text
        }
    }

    class DiffCallback: DiffUtil.ItemCallback<Candidate>() {
        override fun areItemsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Candidate, newItem: Candidate): Boolean {
            return oldItem.text == newItem.text && oldItem.score == newItem.score
        }
    }

    interface Listener {
        fun onItemClicked(candidate: Candidate)
    }
}