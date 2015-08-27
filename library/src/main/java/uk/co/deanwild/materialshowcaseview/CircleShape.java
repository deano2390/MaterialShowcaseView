package uk.co.deanwild.materialshowcaseview;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Circular shape for target.
 */
public class CircleShape implements Shape {

    private int radius = 210;

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

    public static int getPreferredRadius(Rect bounds) {
        return Math.max(bounds.width(), bounds.height()) / 2 + 10;
    }
}
