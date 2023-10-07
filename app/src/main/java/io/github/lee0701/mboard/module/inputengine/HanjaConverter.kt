package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import io.github.lee0701.mboard_lib.conversion.ConversionRequestBroadcaster

class HanjaConverter(
    private val context: Context,
) {
    fun convert(text: String) {
        ConversionRequestBroadcaster.broadcastConvertText(context, text)
    }
}