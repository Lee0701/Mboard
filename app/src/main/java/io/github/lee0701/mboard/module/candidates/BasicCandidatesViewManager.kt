package io.github.lee0701.mboard.module.candidates

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import io.github.lee0701.mboard.databinding.CandidatesViewBinding

class BasicCandidatesViewManager(
    val listener: CandidatesViewManager.Listener
): CandidatesViewManager {

    private var binding: CandidatesViewBinding? = null
    private var adapter: BasicCandidatesAdapter? = null

    override fun initView(context: Context): View {
        val binding = CandidatesViewBinding.inflate(LayoutInflater.from(context), null, false)
        val adapter = BasicCandidatesAdapter(context) { listener.onItemClicked(it) }
        binding.recyclerView.adapter = adapter
        this.binding = binding
        this.adapter = adapter
        return binding.root
    }

    override fun showCandidates(candidates: List<Candidate>) {
        val adapter = this.adapter ?: return
        val binding = this.binding ?: return
        adapter.submitList(candidates) {
            binding.recyclerView.scrollToPosition(0)
        }
    }
}