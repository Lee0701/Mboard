package io.github.lee0701.mboard.module.component

import android.content.Context
import android.view.View

interface Component {

    fun initView(context: Context): View?
    fun updateView()

    fun reset()
}