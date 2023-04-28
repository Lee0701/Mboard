package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import android.view.View
import io.github.lee0701.mboard.module.candidates.CandidatesViewManager
import io.github.lee0701.mboard.module.keyboardview.KeyboardListener

interface SoftInputEngine: InputEngine, KeyboardListener, CandidatesViewManager.Listener {

    val showCandidatesView: Boolean

    fun initView(context: Context): View?
    fun updateView()

    fun getHeight(): Int

}