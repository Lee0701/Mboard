package io.github.lee0701.mboard.module.inputengine

import android.content.Context
import io.github.lee0701.mboard.service.ExternalConversionBroadcaster

class HanjaConverter(
    private val context: Context,
) {
    fun convert(text: String) {
        ExternalConversionBroadcaster.broadcast(context, text)
    }
}