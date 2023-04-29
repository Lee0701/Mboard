package io.github.lee0701.mboard.module.component

import android.content.Context
import android.view.View
import io.github.lee0701.mboard.module.inputengine.CorrectingInputEngine
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.module.keyboardview.CorrectingKeyboardListener
import io.github.lee0701.mboard.module.keyboardview.FlickDirection
import io.github.lee0701.mboard.preset.softkeyboard.Key

class CorrectingKeyboardComponent(
    private val keyboardComponent: KeyboardComponent,
    private val inputEngine: InputEngine,
): InputViewComponent, CorrectingKeyboardListener {

    override fun initView(context: Context): View? = keyboardComponent.initView(context)

    override fun updateView() {
        keyboardComponent.updateView()
    }

    override fun reset() {
        keyboardComponent.updateView()
    }

    override fun onKeyClick(distances: Map<Key, Double>) {
        if(inputEngine is CorrectingInputEngine) {
            val first = distances.keys.first().code
            inputEngine.onKey(
                first,
                distances.mapKeys { it.key.code }, keyboardComponent.currentKeyboardState
            )
        }
    }

    override fun onKeyDown(distances: Map<Key, Double>) {
    }

    override fun onKeyUp(distances: Map<Key, Double>) {
    }

    override fun onKeyClick(code: Int, output: String?) {
        keyboardComponent.onKeyClick(code, output)
    }

    override fun onKeyLongClick(code: Int, output: String?) {
        keyboardComponent.onKeyLongClick(code, output)
    }

    override fun onKeyDown(code: Int, output: String?) {
        keyboardComponent.onKeyDown(code, output)
    }

    override fun onKeyUp(code: Int, output: String?) {
        keyboardComponent.onKeyUp(code, output)
    }

    override fun onKeyFlick(direction: FlickDirection, code: Int, output: String?) {
        keyboardComponent.onKeyFlick(direction, code, output)
    }
}