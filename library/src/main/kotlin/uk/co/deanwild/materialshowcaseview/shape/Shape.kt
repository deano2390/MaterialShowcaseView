package uk.co.deanwild.materialshowcaseview.shape

import android.graphics.Canvas
import android.graphics.Paint
import uk.co.deanwild.materialshowcaseview.target.Target

/**
 * The shape of the "hole" cut out of the showcase mask around the target.
 *
 * Implementations draw the shape centred on (x, y) using the supplied [Paint]
 * (which erases the mask).
 */
interface Shape {

    /** Draw the shape on [canvas], centred on ([x], [y]), using [paint]. */
    fun draw(canvas: Canvas, paint: Paint, x: Int, y: Int)

    /** Width of the shape, excluding padding. */
    fun getWidth(): Int

    /** Height of the shape, excluding padding. */
    fun getHeight(): Int

    /** Update the shape's size from the target, if it adjusts to its target. */
    fun updateTarget(target: Target)

    /** Distance from the centre to the vertical edge of the shape, including padding. */
    fun getTotalRadius(): Int

    /** Extra space between the target and the edge of the shape. */
    fun setPadding(padding: Int)
}
