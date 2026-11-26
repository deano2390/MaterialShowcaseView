package uk.co.deanwild.materialshowcaseview.shape

import android.graphics.Canvas
import android.graphics.Paint
import uk.co.deanwild.materialshowcaseview.target.Target

/** A shape that draws nothing: the whole screen is masked. */
open class NoShape : Shape {
    override fun updateTarget(target: Target) = Unit
    override fun getTotalRadius(): Int = 0
    override fun setPadding(padding: Int) = Unit
    override fun draw(canvas: Canvas, paint: Paint, x: Int, y: Int) = Unit
    override fun getWidth(): Int = 0
    override fun getHeight(): Int = 0
}
