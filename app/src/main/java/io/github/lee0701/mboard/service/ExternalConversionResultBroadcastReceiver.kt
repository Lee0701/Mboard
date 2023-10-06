package io.github.lee0701.mboard.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import io.github.lee0701.mboard.module.candidates.DefaultCandidate
import io.github.lee0701.mboard.module.inputengine.InputEngine

class ExternalConversionResultBroadcastReceiver(
    private val listener: InputEngine.Listener
): BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context ?: return
        intent ?: return
        val array = intent.getStringArrayExtra(EXTRA_TEXT) ?: return
        val result = array
            .map { it.split('\t') }
            .map { (hangul, hanja, extra) -> DefaultCandidate(hanja) }
        listener.onCandidates(result)
    }

    companion object {
        const val ACTION_CONVERT_TEXT_RESULT = "io.github.lee0701.mboard.intent.action.CONVERT_TEXT_RESULT"
        const val PERMISSION_RECEIVE_CONVERTED_TEXT = "io.github.lee0701.mboard.permission.RECEIVE_CONVERTED_TEXT"
        const val EXTRA_TEXT = "io.github.lee0701.mboard.intent.extra.TEXT"
    }
}