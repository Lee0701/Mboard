package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import io.github.lee0701.mboard_lib.conversion.ExternalConversionRequestBroadcaster

class HanjaConverter(
    private val context: Context,
) {
    fun convert(text: String) {
        ExternalConversionRequestBroadcaster.broadcast(context, text)
    }
}