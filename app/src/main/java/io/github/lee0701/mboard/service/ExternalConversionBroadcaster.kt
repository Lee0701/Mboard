package io.github.lee0701.mboard.service

import android.content.Context
import android.content.Intent

object ExternalConversionBroadcaster {

    const val ACTION_CONVERT_TEXT = "io.github.lee0701.mboard.intent.action.CONVERT_TEXT"
    const val PERMISSION_CONVERT_TEXT = "io.github.lee0701.mboard.permission.CONVERT_TEXT"
    const val EXTRA_TEXT = "io.github.lee0701.mboard.intent.extra.TEXT"

    fun broadcast(context: Context, text: String) {
        val intent = Intent().apply {
            action = ACTION_CONVERT_TEXT
            putExtra(EXTRA_TEXT, text)
        }
        context.sendBroadcast(intent, PERMISSION_CONVERT_TEXT)
    }
}