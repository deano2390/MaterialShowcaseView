package uk.co.deanwild.materialshowcaseview;

import android.graphics.Canvas;
import android.graphics.Paint;

/**
 * Specifies a shape of the target (e.g circle, rectangle)
 */
public interface Shape {

    void updateTarget(Target target);

    void draw(Canvas canvas, Paint paint, int x, int y);

}
