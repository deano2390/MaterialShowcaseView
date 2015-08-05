package uk.co.deanwild.materialshowcaseview;

import android.app.Activity;
import android.graphics.Point;
import android.view.View;

/**
 * Created by deanwild on 04/08/15.
 */
public class ViewTarget implements Target {

    private final View mView;

    public ViewTarget(View view) {
        mView = view;
    }

    public ViewTarget(int viewId, Activity activity) {
        mView = activity.findViewById(viewId);
    }

    @Override
    public Point getPoint() {
        int[] location = new int[2];
        mView.getLocationInWindow(location);
        int x = location[0] + mView.getWidth() / 2;
        int y = location[1] + mView.getHeight() / 2;
        return new Point(x, y);
    }

    @Override
    public int getRadius() {

        int radius = 200;

        if (mView != null) {

            if (mView.getMeasuredHeight() > mView.getMeasuredWidth()) {
                radius = mView.getMeasuredHeight() / 2;
            }else{
                radius =  mView.getMeasuredWidth() / 2;
            }

            radius += 10; // add a 10 pixel padding to circle
        }

        return radius;
    }
}
