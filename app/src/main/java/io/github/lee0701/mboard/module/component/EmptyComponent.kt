package io.github.lee0701.mboard.module.component

import android.content.Context
import android.view.View

object EmptyComponent: InputViewComponent {
    override fun initView(context: Context): View? = null

    override fun updateView() = Unit

    override fun reset() = Unit
}