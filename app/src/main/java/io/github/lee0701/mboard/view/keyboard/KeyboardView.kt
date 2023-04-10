package io.github.lee0701.mboard.view.keyboard

import android.content.Context
import android.graphics.*
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.util.TypedValue
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.graphics.drawable.toBitmap
import com.google.android.material.color.DynamicColors
import io.github.lee0701.mboard.R
import io.github.lee0701.mboard.module.KeyType
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
    private val keyBackgrounds: Map<KeyType, Drawable>
    private val keyIconTints: Map<KeyType, Int>
    private val keyLabelTextColors: Map<KeyType, Int>
    private val keyLabelTextSizes: Map<KeyType, Float>

    private val keyMarginHorizontal: Float
    private val keyMarginVertical: Float

    private val handler = Handler(Looper.getMainLooper())
    private val pointers = mutableMapOf<Int, TouchPointer>()
    private var keyPopup: KeyPopup? = null

    private val showKeyPopups = true
    private val longPressDuration = 500L
    private val repeatInterval = 50L

    init {
        textPaint.textAlign = Paint.Align.CENTER

        val keyboardContext = DynamicColors.wrapContextIfAvailable(context, theme.keyboardBackground)
        keyboardContext.theme.resolveAttribute(R.attr.background, typedValue, true)
        val background = ContextCompat.getDrawable(keyboardContext, typedValue.resourceId) ?: ColorDrawable(Color.WHITE)
        keyboardContext.theme.resolveAttribute(R.attr.backgroundTint, typedValue, true)
        val backgroundTint = ContextCompat.getColor(keyboardContext, typedValue.resourceId)
        DrawableCompat.setTint(background, backgroundTint)
        this.keyboardBackground = background

        val keyContexts = theme.keyBackground.mapValues { (_, id) ->
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
        keyPopup = KeyPopup(context)
    }

    fun cacheKeys() {
        val rowHeight = keyboardHeight / keyboard.rows.size
        keyboard.rows.forEachIndexed { j, row ->
            val keyWidths = row.keys.map { it.width }.sum() + row.padding*2
            val keyWidthUnit = keyboardWidth / keyWidths
            var x = row.padding * keyWidthUnit
            val y = j * rowHeight
            row.keys.forEachIndexed { i, key ->
                val width = keyWidthUnit * key.width
                val height = rowHeight
                val icon = theme.keyIcon[key.iconType]?.let { ContextCompat.getDrawable(context, it) }
                cachedKeys += CachedKey(key, x.roundToInt(), y.roundToInt(), width.roundToInt(), height.roundToInt(), icon)
                x += width
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        if(canvas == null) return
        getLocalVisibleRect(rect)
        val bitmapCache = mutableMapOf<Pair<Float, Float>, Bitmap>()

        // Draw keyboard background
        canvas.drawBitmap(keyboardBackground.toBitmap(rect.width(), rect.height()), 0f, 0f, bitmapPaint)

        // Draw key backgrounds
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

        // Draw key foregrounds
        cachedKeys.forEach { key ->
            val baseX = key.x + key.width/2
            val baseY = key.y + key.height/2
            val tint = keyIconTints[key.key.type]
            if(key.icon != null && tint != null) {
                DrawableCompat.setTint(key.icon, tint)
                val bitmap = key.icon.toBitmap()
                val x = baseX - bitmap.width/2
                val y = baseY - bitmap.height/2
                canvas.drawBitmap(bitmap, x.toFloat(), y.toFloat(), bitmapPaint)
            }
            val textSize = keyLabelTextSizes[key.key.type]
            val textColor = keyLabelTextColors[key.key.type]
            if(key.key.label != null && textSize != null && textColor != null) {
                textPaint.color = textColor
                textPaint.textSize = textSize
                val x = baseX.toFloat()
                val y = baseY - (textPaint.descent() + textPaint.ascent())/2
                canvas.drawText(key.key.label, x, y, textPaint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if(event == null) return super.onTouchEvent(event)

        val pointerId = event.getPointerId(event.actionIndex)
        val x = event.getX(event.actionIndex).roundToInt()
        val y = event.getY(event.actionIndex).roundToInt()

        val key = findKey(x, y) ?: return super.onTouchEvent(event)

        when(event.actionMasked) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_POINTER_DOWN -> {
                val pointer = TouchPointer(x, y, key)

                this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
                if(showKeyPopups &&
                    (key.key.type == KeyType.Alphanumeric || key.key.type == KeyType.AlphanumericAlt)) {
                    keyPopup?.apply {
                        show(this@KeyboardView, key.x + key.width/2, key.y + key.height/2)
                    }
                } else {
                    keyPopup?.cancel()
                }
                fun repeater() {
                    listener.onKeyClick(key.key.code, key.key.output)
                    handler.postDelayed({ repeater() }, repeatInterval)
                }
                handler.postDelayed({
                    if(key.key.repeatable) repeater()
                }, longPressDuration)
                listener.onKeyDown(key.key.code, key.key.output)
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_POINTER_UP -> {
                handler.removeCallbacksAndMessages(null)
                keyPopup?.hide()
                listener.onKeyUp(key.key.code, key.key.output)
                listener.onKeyClick(key.key.code, key.key.output)
                performClick()
            }
        }

        return true
    }

    fun updateLabelsAndIcons(labels: Map<Int, CharSequence>, icons: Map<Int, Drawable>) {
        val cachedKeys = this.cachedKeys.toList()
        this.cachedKeys.clear()
        this.cachedKeys += cachedKeys.map { key ->
            if(key.icon != null) {
                key.copy(icon = icons[key.key.code] ?: key.icon)
            } else {
                key.copy(key = key.key.copy(label = labels[key.key.code]?.toString()))
            }
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(keyboardWidth.roundToInt(), keyboardHeight.roundToInt())
    }

    private fun dipToPixel(dip: Float): Float {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dip, context.resources.displayMetrics)
    }

    private fun findKey(x: Int, y: Int): CachedKey? {
        cachedKeys.forEach { key ->
            if(x in key.x until key.x+key.width) {
                if(y in key.y until key.y+key.height) {
                    return key
                }
            }
        }
        return null
    }

    data class CachedKey(
        val key: Key,
        val x: Int,
        val y: Int,
        val width: Int,
        val height: Int,
        val icon: Drawable?,
    )

    data class TouchPointer(
        val initialX: Int,
        val initialY: Int,
        val key: CachedKey,
    )

}