package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import uk.co.deanwild.materialshowcaseview.target.Target;

public class OvalShape implements Shape {
    private int radius;
    private boolean adjustToTarget;

    public OvalShape() {
        this.radius = 200;
        this.adjustToTarget = true;
    }

    public OvalShape(int radius) {
        this.radius = 200;
        this.adjustToTarget = true;
        this.radius = radius;
    }

    public OvalShape(Rect bounds) {
        this(getPreferredRadius(bounds));
    }

    public OvalShape(Target target) {
        this(target.getBounds());
    }

    public static int getPreferredRadius(Rect bounds) {
        return Math.max(bounds.width(), bounds.height()) / 2;
    }

    public boolean isAdjustToTarget() {
        return this.adjustToTarget;
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    public int getRadius() {
        return this.radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
        if (this.radius > 0) {
            float rad = (float) (this.radius + padding);
            RectF rectF = new RectF(x - rad, y - rad / 2, x + rad, y + rad / 2);
            canvas.drawOval(rectF, paint);
        }

    }

    public void updateTarget(Target target) {
        if (this.adjustToTarget) {
            this.radius = getPreferredRadius(target.getBounds());
        }

    }

    public int getWidth() {
        return this.radius * 2;
    }

    public int getHeight() {
        return this.radius;
    }
}

