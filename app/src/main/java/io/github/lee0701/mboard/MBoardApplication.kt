package io.github.lee0701.mboard

import android.app.Application
import com.google.android.material.color.DynamicColors

class MBoardApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}