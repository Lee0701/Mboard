package io.github.lee0701.mboard.settings

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.inputengine.InputEngine
import io.github.lee0701.mboard.module.keyboardview.FlickDirection
import io.github.lee0701.mboard.module.keyboardview.KeyboardListener
import io.github.lee0701.mboard.preset.softkeyboard.Key

class KeyboardLayoutSettingsActivity: AppCompatActivity() {

    private val fileName: String by lazy { intent.getStringExtra("fileName") ?: "default.yaml" }
    private val template: String by lazy { intent.getStringExtra("template") ?: "default.yaml" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_keyboard_layout_settings)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, KeyboardLayoutSettingsFragment(fileName, template))
            .commit()
        supportActionBar?.setDisplayShowHomeEnabled(true)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    companion object {
        val emptyKeyboardListener = object: KeyboardListener {
            override fun onKeyClick(key: Key) = Unit
            override fun onKeyLongClick(key: Key) = Unit
            override fun onKeyDown(key: Key) = Unit
            override fun onKeyUp(key: Key) = Unit
            override fun onKeyFlick(direction: FlickDirection, key: Key) = Unit
        }

        val emptyInputEngineListener = object: InputEngine.Listener {
            override fun onComposingText(text: CharSequence) = Unit
            override fun onFinishComposing() = Unit
            override fun onCommitText(text: CharSequence) = Unit
            override fun onDeleteText(beforeLength: Int, afterLength: Int) = Unit
            override fun onCandidates(list: List<Candidate>) = Unit
            override fun onSystemKey(code: Int): Boolean = false
            override fun onEditorAction(code: Int) = Unit
        }

        val emptyCandidateListener = object: CandidateListener {
            override fun onCandidateItemClicked(candidate: Candidate) = Unit
        }
    }

}