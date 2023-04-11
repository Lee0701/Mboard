package io.github.lee0701.mboard

import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors

class MBoardApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}