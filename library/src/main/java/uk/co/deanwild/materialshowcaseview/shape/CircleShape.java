package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import uk.co.deanwild.materialshowcaseview.Target;

/**
 * Circular shape for target.
 */
public class CircleShape implements Shape {

    private int radius = 200;

    public CircleShape(int radius) {
        this.radius = radius;
    }

    public CircleShape(Rect bounds) {
        this(getPreferredRadius(bounds));
    }

    public CircleShape(Target target) {
        this(target.getBounds());
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y) {
        canvas.drawCircle(x, y, radius, paint);
    }

    @Override
    public void updateTarget(Target target) {
        radius = getPreferredRadius(target.getBounds());
    }

    @Override
    public int getWidth() {
        return radius * 2;
    }

    @Override
    public int getHeight() {
        return radius * 2;
    }

    public static int getPreferredRadius(Rect bounds) {
        return Math.max(bounds.width(), bounds.height()) / 2;
    }
}
