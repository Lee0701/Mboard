package io.github.lee0701.mboard.module.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import io.github.lee0701.mboard.databinding.ComponentCandidatesBinding
import io.github.lee0701.mboard.module.candidates.BasicCandidatesAdapter
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener

class CandidatesComponent(
    private val height: Int,
    private val disableTouch: Boolean = false,
): InputViewComponent {

    var listener: CandidateListener? = null
    private var binding: ComponentCandidatesBinding? = null
    private var adapter: BasicCandidatesAdapter? = null

    override fun initView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val binding = ComponentCandidatesBinding.inflate(inflater, null, false)
        binding.root.layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, height)
        val adapter = BasicCandidatesAdapter(context) { listener?.onItemClicked(it) }
        binding.recyclerView.adapter = adapter
        this.binding = binding
        this.adapter = adapter
        return binding.root
    }

    override fun updateView() {
    }

    override fun reset() {
    }

    fun showCandidates(candidates: List<Candidate>) {
        val adapter = this.adapter ?: return
        val binding = this.binding ?: return
        adapter.submitList(candidates) {
            binding.recyclerView.scrollToPosition(0)
        }
    }
}