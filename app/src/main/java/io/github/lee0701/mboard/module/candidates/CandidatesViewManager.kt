package io.github.lee0701.mboard.module.candidates

import android.content.Context
import android.view.View

interface CandidatesViewManager {

    fun initView(context: Context): View?
    fun showCandidates(candidates: List<Candidate>)

    interface Listener {
        fun onItemClicked(candidate: Candidate)
    }
}