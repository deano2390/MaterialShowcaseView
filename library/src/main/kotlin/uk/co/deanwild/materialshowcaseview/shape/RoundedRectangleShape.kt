package uk.co.deanwild.materialshowcaseview.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import uk.co.deanwild.materialshowcaseview.target.Target

/**
 * A rectangle with rounded corners matching the target's bounds, optionally spanning the full
 * width of the screen.
 *
 * Based on the shape contributed by Daniel James in #204.
 */
open class RoundedRectangleShape @JvmOverloads constructor(
    bounds: Rect,
    private val fullWidth: Boolean = false,
    /** Corner radius in pixels. */
    open var cornerRadius: Int = DEFAULT_CORNER_RADIUS,
) : Shape {

    /** When true (the default) the rectangle follows the target's size. */
    open var isAdjustToTarget: Boolean = true

    private var shapeWidth = if (fullWidth) FULL_WIDTH else bounds.width()
    private var shapeHeight = bounds.height()
    private var shapePadding = 0

    constructor(width: Int, height: Int) : this(Rect(0, 0, width, height))

    override fun draw(canvas: Canvas, paint: Paint, x: Int, y: Int) {
        if (shapeWidth <= 0 || shapeHeight <= 0) return
        val r = cornerRadius.toFloat()
        canvas.drawRoundRect(
            (x - shapeWidth / 2 - shapePadding).toFloat(),
            (y - shapeHeight / 2 - shapePadding).toFloat(),
            (x + shapeWidth / 2 + shapePadding).toFloat(),
            (y + shapeHeight / 2 + shapePadding).toFloat(),
            r,
            r,
            paint,
        )
    }

    override fun updateTarget(target: Target) {
        if (isAdjustToTarget) {
            val bounds = target.getBounds()
            shapeHeight = bounds.height()
            shapeWidth = if (fullWidth) FULL_WIDTH else bounds.width()
        }
    }

    override fun getTotalRadius(): Int = shapeHeight / 2 + shapePadding

    override fun setPadding(padding: Int) {
        shapePadding = padding
    }

    override fun getWidth(): Int = shapeWidth

    override fun getHeight(): Int = shapeHeight

    private companion object {
        const val DEFAULT_CORNER_RADIUS = 30
        const val FULL_WIDTH = Int.MAX_VALUE / 2
    }
}
