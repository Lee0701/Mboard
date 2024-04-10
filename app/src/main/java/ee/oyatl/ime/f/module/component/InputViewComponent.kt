package ee.oyatl.ime.f.module.component

import android.content.Context
import android.view.View

interface InputViewComponent {

    fun initView(context: Context): View?
    fun updateView()

    fun reset()
}