package uk.co.deanwild.materialshowcaseview.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import uk.co.deanwild.materialshowcaseview.target.Target

/** A rectangle matching the target's bounds, optionally spanning the full width of the screen. */
open class RectangleShape private constructor(
    private var shapeWidth: Int,
    private var shapeHeight: Int,
    private val fullWidth: Boolean,
) : Shape {

    /** When true (the default) the rectangle follows the target's size. */
    open var isAdjustToTarget: Boolean = true

    private var shapePadding = 0
    private var rect = Rect()

    constructor(width: Int, height: Int) : this(width, height, false)

    constructor(bounds: Rect) : this(bounds, false)

    constructor(bounds: Rect, fullWidth: Boolean) :
        this(if (fullWidth) FULL_WIDTH else bounds.width(), bounds.height(), fullWidth)

    init {
        updateRect()
    }

    private fun updateRect() {
        rect = Rect(-shapeWidth / 2, -shapeHeight / 2, shapeWidth / 2, shapeHeight / 2)
    }

    override fun draw(canvas: Canvas, paint: Paint, x: Int, y: Int) {
        if (!rect.isEmpty) {
            canvas.drawRect(
                (rect.left + x - shapePadding).toFloat(),
                (rect.top + y - shapePadding).toFloat(),
                (rect.right + x + shapePadding).toFloat(),
                (rect.bottom + y + shapePadding).toFloat(),
                paint,
            )
        }
    }

    override fun updateTarget(target: Target) {
        if (isAdjustToTarget) {
            val bounds = target.getBounds()
            shapeHeight = bounds.height()
            shapeWidth = if (fullWidth) FULL_WIDTH else bounds.width()
            updateRect()
        }
    }

    override fun getTotalRadius(): Int = shapeHeight / 2 + shapePadding

    override fun setPadding(padding: Int) {
        shapePadding = padding
    }

    override fun getWidth(): Int = shapeWidth

    override fun getHeight(): Int = shapeHeight

    // Kotlin source compatibility with 1.x, where these were Java getters/setters
    // callable as methods from Kotlin. Hidden from Java, which uses the property accessors.

    @JvmSynthetic @JvmName("isAdjustToTarget\$compat")
    @Deprecated("Use the isAdjustToTarget property", ReplaceWith("isAdjustToTarget"))
    fun isAdjustToTarget(): Boolean = isAdjustToTarget

    @JvmSynthetic @JvmName("setAdjustToTarget\$compat")
    @Deprecated("Use the isAdjustToTarget property", ReplaceWith("isAdjustToTarget = value"))
    fun setAdjustToTarget(value: Boolean) {
        isAdjustToTarget = value
    }

    private companion object {
        /** Wide enough to span any screen without overflowing when offset by the target position. */
        const val FULL_WIDTH = Int.MAX_VALUE / 2
    }
}
