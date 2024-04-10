package ee.oyatl.ime.f

import androidx.multidex.MultiDexApplication
import com.google.android.material.color.DynamicColors

class MBoardApplication: MultiDexApplication() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}