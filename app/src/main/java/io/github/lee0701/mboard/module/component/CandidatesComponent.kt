package io.github.lee0701.mboard.module.component

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams
import androidx.appcompat.view.ContextThemeWrapper
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.ViewCompat
import androidx.preference.PreferenceManager
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.databinding.ComponentCandidatesBinding
import io.github.lee0701.mboard.module.candidates.BasicCandidatesAdapter
import io.github.lee0701.mboard.module.candidates.Candidate
import io.github.lee0701.mboard.module.candidates.CandidateListener
import io.github.lee0701.mboard.module.keyboardview.Themes
import io.github.lee0701.mboard.settings.TouchInterceptingFrameLayout

class CandidatesComponent(
    private val width: Int,
    private val height: Int,
    private val disableTouch: Boolean = false,
): InputViewComponent {

    var listener: CandidateListener? = null
    private var binding: ComponentCandidatesBinding? = null
    private var adapter: BasicCandidatesAdapter? = null

    private val typedValue = TypedValue()

    override fun initView(context: Context): View {
        val inflater = LayoutInflater.from(context)
        val preferences = PreferenceManager.getDefaultSharedPreferences(context)
        val binding = ComponentCandidatesBinding.inflate(inflater)

        val name = preferences.getString("appearance_theme", "theme_dynamic")
        val theme = Themes.ofName(name)

        val backgroundContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground).let {
            if(it == context) ContextThemeWrapper(context, theme.keyboardBackground) else it
        }
        backgroundContext.theme.resolveAttribute(R.attr.background, typedValue, true)
        val background = ContextCompat.getDrawable(backgroundContext, typedValue.resourceId) ?: ColorDrawable(Color.WHITE)
        backgroundContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
        val backgroundTint = ContextCompat.getColor(backgroundContext, typedValue.resourceId)
        DrawableCompat.setTint(background, backgroundTint)
        ViewCompat.setBackground(binding.root, background)

        binding.root.layoutParams = LayoutParams(width, height)
        val adapter = BasicCandidatesAdapter(context) { listener?.onItemClicked(it) }
        binding.recyclerView.adapter = adapter

        val wrapper = TouchInterceptingFrameLayout(context, null, disableTouch)
        wrapper.addView(binding.root)

        this.binding = binding
        this.adapter = adapter
        return wrapper
    }

    override fun updateView() {
    }

    override fun reset() {
        adapter?.submitList(listOf())
    }

    fun showCandidates(candidates: List<Candidate>) {
        val adapter = this.adapter ?: return
        val binding = this.binding ?: return
        adapter.submitList(candidates) {
            binding.recyclerView.scrollToPosition(0)
        }
    }
}