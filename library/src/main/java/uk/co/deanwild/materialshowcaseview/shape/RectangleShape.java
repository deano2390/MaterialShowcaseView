package uk.co.deanwild.materialshowcaseview.shape;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

import uk.co.deanwild.materialshowcaseview.Target;

public class RectangleShape implements Shape {

    private boolean fullWidth;

    private int width = 0;
    private int height = 0;

    private Rect rect;

    public RectangleShape(Rect bounds) {
        this(bounds, false);
    }

    public RectangleShape(Rect bounds, boolean fullWidth) {
        this.fullWidth = fullWidth;
        height = bounds.height();
        if (fullWidth)
            width = Integer.MAX_VALUE;
        else width = bounds.width();
        init();
    }

    private void init() {
        rect = new Rect(- width / 2, - height / 2, width / 2, height / 2);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y) {
        rect.offset(x, y);
        canvas.drawRect(rect, paint);
        rect.offset(-x, -y);
    }

    @Override
    public void updateTarget(Target target) {
        Rect bounds = target.getBounds();
        height = bounds.height();
        if (fullWidth)
            width = Integer.MAX_VALUE;
        else width = bounds.width();
        init();
    }

    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }
}