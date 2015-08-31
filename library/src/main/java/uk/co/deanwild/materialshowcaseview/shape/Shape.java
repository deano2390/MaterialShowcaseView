package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;

import uk.co.deanwild.materialshowcaseview.Target;

/**
 * Specifies a shape of the target (e.g circle, rectangle)
 */
public interface Shape {

    void updateTarget(Target target);

    void draw(Canvas canvas, Paint paint, int x, int y);

    int getWidth();

    int getHeight();

}
