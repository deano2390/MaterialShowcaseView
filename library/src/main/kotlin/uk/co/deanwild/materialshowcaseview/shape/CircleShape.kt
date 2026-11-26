package uk.co.deanwild.materialshowcaseview.shape

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import uk.co.deanwild.materialshowcaseview.target.Target
import kotlin.math.max

/** A circle around the target. */
open class CircleShape @JvmOverloads constructor(radius: Int = DEFAULT_RADIUS) : Shape {

    open var radius: Int = radius

    /** When true (the default) the radius follows the target's size. */
    open var isAdjustToTarget: Boolean = true

    private var shapePadding = 0

    constructor(bounds: Rect) : this(getPreferredRadius(bounds))

    constructor(target: Target) : this(target.getBounds())

    override fun draw(canvas: Canvas, paint: Paint, x: Int, y: Int) {
        if (radius > 0) {
            canvas.drawCircle(x.toFloat(), y.toFloat(), (radius + shapePadding).toFloat(), paint)
        }
    }

    override fun updateTarget(target: Target) {
        if (isAdjustToTarget) radius = getPreferredRadius(target.getBounds())
    }

    override fun getTotalRadius(): Int = radius + shapePadding

    override fun setPadding(padding: Int) {
        shapePadding = padding
    }

    override fun getWidth(): Int = radius * 2

    override fun getHeight(): Int = radius * 2

    // Kotlin source compatibility with 1.x, where these were Java getters/setters
    // callable as methods from Kotlin. Hidden from Java, which uses the property accessors.

    @JvmSynthetic @JvmName("getRadius\$compat")
    @Deprecated("Use the radius property", ReplaceWith("radius"))
    fun getRadius(): Int = radius

    @JvmSynthetic @JvmName("setRadius\$compat")
    @Deprecated("Use the radius property", ReplaceWith("radius = value"))
    fun setRadius(value: Int) {
        radius = value
    }

    @JvmSynthetic @JvmName("isAdjustToTarget\$compat")
    @Deprecated("Use the isAdjustToTarget property", ReplaceWith("isAdjustToTarget"))
    fun isAdjustToTarget(): Boolean = isAdjustToTarget

    @JvmSynthetic @JvmName("setAdjustToTarget\$compat")
    @Deprecated("Use the isAdjustToTarget property", ReplaceWith("isAdjustToTarget = value"))
    fun setAdjustToTarget(value: Boolean) {
        isAdjustToTarget = value
    }

    companion object {
        private const val DEFAULT_RADIUS = 200

        @JvmStatic
        fun getPreferredRadius(bounds: Rect): Int = max(bounds.width(), bounds.height()) / 2
    }
}
