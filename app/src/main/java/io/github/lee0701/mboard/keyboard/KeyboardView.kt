package io.github.lee0701.mboard.keyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import kotlin.math.roundToInt

class KeyboardView(
    context: Context,
    attrs: AttributeSet?,
    private val keyboard: Keyboard,
    private val theme: Theme,
    private val listener: Keyboard.Listener,
): View(context, attrs) {
    private val keyboardWidth = context.resources.displayMetrics.widthPixels.toFloat()
    private val keyboardHeight = dipToPixel(keyboard.height)

    private val rect = Rect()
    private val typedValue = TypedValue()
    private val bitmapPaint = Paint()
    private val textPaint = Paint()

    private val cachedKeys = mutableListOf<CachedKey>()

    private val keyboardBackground: Drawable
    private val keyBackgrounds: Map<Key.Type, Drawable>
    private val keyIconTints: Map<Key.Type, Int>
    private val keyLabelTextColors: Map<Key.Type, Int>
    private val keyLabelTextSizes: Map<Key.Type, Float>

    private val keyMarginHorizontal: Float
    private val keyMarginVertical: Float

    init {
        textPaint.textAlign = Paint.Align.CENTER

        val keyboardContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboard)
        keyboardContext.theme.resolveAttribute(R.attr.background, typedValue, true)
        val background = ContextCompat.getDrawable(keyboardContext, typedValue.resourceId) ?: ColorDrawable(Color.WHITE)
        keyboardContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
        val backgroundTint = ContextCompat.getColor(keyboardContext, typedValue.resourceId)
        DrawableCompat.setTint(background, backgroundTint)
        this.keyboardBackground = background

        val keyContexts = theme.key.mapValues { (_, id) ->
            DynamicColors.wrapContextIfAvailable(context, id)
        }
        keyBackgrounds = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(R.attr.background, typedValue, true)
            val keyBackground = ContextCompat.getDrawable(keyContext, typedValue.resourceId) ?: ColorDrawable(Color.TRANSPARENT)
            keyContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
            val keyBackgroundTint = ContextCompat.getColor(keyContext, typedValue.resourceId)
            DrawableCompat.setTint(keyBackground, keyBackgroundTint)
            keyBackground
        }
        keyIconTints = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(R.attr.iconTint, typedValue, true)
            ContextCompat.getColor(keyContext, typedValue.resourceId)
        }
        keyLabelTextColors = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(android.R.attr.textColor, typedValue, true)
            ContextCompat.getColor(keyContext, typedValue.resourceId)
        }
        keyLabelTextSizes = keyContexts.mapValues { (_, keyContext) ->
            keyContext.theme.resolveAttribute(android.R.attr.textSize, typedValue, true)
            context.resources.getDimension(typedValue.resourceId)
        }
        keyMarginHorizontal = resources.getDimension(R.dimen.key_margin_horizontal)
        keyMarginVertical = resources.getDimension(R.dimen.key_margin_vertical)

        cacheKeys()
    }

    private fun cacheKeys() {
        val rowHeight = keyboardHeight / keyboard.rows.size
        keyboard.rows.forEachIndexed { j, row ->
            val keyWidths = row.keys.map { it.width }.sum() + row.padding*2
            val keyWidthUnit = keyboardWidth / keyWidths
            var x = row.padding * keyWidthUnit
            val y = j * rowHeight
            row.keys.forEachIndexed { i, key ->
                val width = keyWidthUnit * key.width
                val height = rowHeight
                val icon = key.icon?.let { ContextCompat.getDrawable(context, it) }
                cachedKeys += CachedKey(key, x, y, width, height, icon)
                x += width
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        getLocalVisibleRect(rect)
        canvas.drawBitmap(keyboardBackground.toBitmap(rect.width(), rect.height()), 0f, 0f, bitmapPaint)

        val bitmapCache = mutableMapOf<Pair<Float, Float>, Bitmap>()
        cachedKeys.forEach { key ->
            val background = keyBackgrounds[key.key.type]
            if(background != null) {
                val x = key.x + keyMarginHorizontal
                val y = key.y + keyMarginVertical
                val width = key.width - keyMarginHorizontal*2
                val height = key.height - keyMarginVertical*2
                val bitmap = bitmapCache.getOrPut(width to height) {
                    background.toBitmap(width.roundToInt(), height.roundToInt()) }
                canvas.drawBitmap(bitmap, x, y, bitmapPaint)
            }
        }

        cachedKeys.forEach { key ->
            val baseX = key.x + key.width/2
            val baseY = key.y + key.height/2
            val tint = keyIconTints[key.key.type]
            if(key.icon != null && tint != null) {
                DrawableCompat.setTint(key.icon, tint)
                val bitmap = key.icon.toBitmap()
                val x = baseX - bitmap.width/2
                val y = baseY - bitmap.height/2
                canvas.drawBitmap(bitmap, x, y, bitmapPaint)
            }
            val textSize = keyLabelTextSizes[key.key.type]
            val textColor = keyLabelTextColors[key.key.type]
            if(key.key.label != null && textSize != null && textColor != null) {
                textPaint.color = textColor
                textPaint.textSize = textSize
                val x = baseX
                val y = baseY - (textPaint.descent() + textPaint.ascent())/2
                canvas.drawText(key.key.label, x, y, textPaint)
            }
        }
    }

    private fun onDrawKeyBackground(canvas: Canvas, key: Key) {

    }

    private fun onDrawKeyForeground(canvas: Canvas, key: Key) {

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(keyboardWidth.roundToInt(), keyboardHeight.roundToInt())
    }

    private fun dipToPixel(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics)
    }

    data class CachedKey(
        val key: Key,
        val x: Float,
        val y: Float,
        val width: Float,
        val height: Float,
        val icon: Drawable?,
    )
}