package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

import uk.co.deanwild.materialshowcaseview.target.Target;

/**
 * The shape of the "hole" cut out of the showcase mask around the target. Implementations draw
 * the shape centred on (x, y) using the supplied Paint, which erases the mask.
 * <p>
 * Declared in Java on purpose, so Kotlin implementations written against 1.x keep compiling.
 */
public interface Shape {

    /** Draw the shape on the canvas, centred on (x, y), using the paint. */
    void draw(Canvas canvas, Paint paint, int x, int y);

    /** Width of the shape, excluding padding. */
    int getWidth();

    /** Height of the shape, excluding padding. */
    int getHeight();

    /** Update the shape's size from the target, if it adjusts to its target. */
    void updateTarget(Target target);

    /** Distance from the centre to the vertical edge of the shape, including padding. */
    int getTotalRadius();

    /** Extra space between the target and the edge of the shape. */
    void setPadding(int padding);
}
