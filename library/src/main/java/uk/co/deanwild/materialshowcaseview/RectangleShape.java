package uk.co.deanwild.materialshowcaseview;


import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;

public class RectangleShape implements Shape {

    private Activity activity;
    private Rect rect;
    private boolean fullWidth;


    public RectangleShape(Rect bounds) {
        this(bounds, null, false);
    }

    public RectangleShape(Rect bounds, Activity activity, boolean fullWidth) {
        this.activity = activity;
        this.fullWidth = fullWidth;
        this.rect = getPreferredBounds(bounds, null, false);
    }

    @Override
    public void draw(Canvas canvas, Paint paint, int x, int y) {
        canvas.drawRect(rect, paint);
    }

    @Override
    public void updateTarget(Target target) {
        rect = getPreferredBounds(target.getBounds(), activity, fullWidth);
    }

    public static Rect getPreferredBounds(Rect bounds, Activity activity,  boolean fullWidth) {
        Rect rect = new Rect(bounds.left - 10, bounds.top - 10, bounds.right + 10, bounds.bottom + 10);
        if (activity != null && fullWidth) {
            rect.left = 0;
            rect.right = getDisplayMetrics(activity).widthPixels;
        }
        return rect;
    }

    public static DisplayMetrics getDisplayMetrics(Activity context) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        context.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }
}