package io.github.lee0701.mboard.input

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.view.View
import io.github.lee0701.mboard.view.candidates.BasicCandidatesViewManager
import io.github.lee0701.mboard.view.keyboard.KeyboardListener

interface SoftInputEngine: InputEngine, KeyboardListener, BasicCandidatesViewManager.Listener {

    val showCandidatesView: Boolean

    fun initView(context: Context): View?
    fun updateView()

    fun getHeight(): Int

}