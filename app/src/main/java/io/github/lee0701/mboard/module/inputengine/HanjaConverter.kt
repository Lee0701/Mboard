package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import ee.oyatl.ime.f.fusion.ConversionRequestBroadcaster

class HanjaConverter(
    private val context: Context,
) {
    fun convert(text: String) {
        ConversionRequestBroadcaster.broadcast(context, text)
    }
}