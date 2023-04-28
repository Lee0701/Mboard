package io.github.lee0701.mboard.module.component

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import io.github.lee0701.mboard.databinding.ComponentCandidatesBinding
import io.github.lee0701.mboard.module.candidates.BasicCandidatesAdapter
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.settings.TouchInterceptingFrameLayout

class CandidatesComponent(
    private val width: Int,
    private val height: Int,
    private val disableTouch: Boolean = false,
): InputViewComponent {

    var listener: CandidateListener? = null
    private var binding: ComponentCandidatesBinding? = null
    private var adapter: BasicCandidatesAdapter? = null

    override fun initView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val binding =
            ComponentCandidatesBinding.inflate(inflater)

        binding.root.layoutParams = LayoutParams(width, height)
        val adapter = BasicCandidatesAdapter(context) { listener?.onItemClicked(it) }
        binding.recyclerView.adapter = adapter

        val wrapper = TouchInterceptingFrameLayout(context, null, disableTouch)
        wrapper.addView(binding.root)

        this.binding = binding
        this.adapter = adapter
        return wrapper
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