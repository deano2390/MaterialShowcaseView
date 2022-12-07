package uk.co.deanwild.materialshowcaseview.shape;


import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Build;

import uk.co.deanwild.materialshowcaseview.target.Target;

public class RoundedRectangleShape implements Shape {

    private boolean fullWidth = false;

    private int width = 0;
    private int height = 0;
    private boolean adjustToTarget = true;

    private Rect rect;
    private int padding;
    private int rounding;

    public RoundedRectangleShape(int width, int height) {
        this.width = width;
        this.height = height;
        init();
    }

    public RoundedRectangleShape(Rect bounds) {
        this(bounds, false, 6);
    }

    public RoundedRectangleShape(Rect bounds, boolean fullWidth, int rounding) {
        this.rounding = rounding;
        this.fullWidth = fullWidth;
        height = bounds.height();
        if (fullWidth)
            width = Integer.MAX_VALUE;
        else width = bounds.width();
        init();
    }

    public boolean isAdjustToTarget() {
        return adjustToTarget;
    }

    public void setAdjustToTarget(boolean adjustToTarget) {
        this.adjustToTarget = adjustToTarget;
    }

    private void init() {
        rect = new Rect(-width / 2, -height / 2, width / 2, height / 2);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y) {
        if (!rect.isEmpty()) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                canvas.drawRoundRect(
                        rect.left + x - padding,
                        rect.top + y - padding,
                        rect.right + x + padding,
                        rect.bottom + y + padding,
                        rounding,
                        rounding,
                        paint
                );
            }else{
                canvas.drawRect(
                        rect.left + x - padding,
                        rect.top + y - padding,
                        rect.right + x + padding,
                        rect.bottom + y + padding,
                        paint
                );
            }
        }
    }


    public void setRounding(int r){
        this.rounding = r;
    }

    @Override
    public void updateTarget(Target target) {
        if (adjustToTarget) {
            Rect bounds = target.getBounds();
            height = bounds.height();
            if (fullWidth)
                width = Integer.MAX_VALUE;
            else width = bounds.width();
            init();
        }
    }

    @Override
    public int getTotalRadius() {
        return (height / 2) + padding;
    }

    @Override
    public void setPadding(int padding) {
        this.padding = padding;
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