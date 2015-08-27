package uk.co.deanwild.materialshowcaseview;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Created by deanwild on 04/08/15.
 */
public interface Target {
    Target NONE = new Target() {
        @Override
        public Point getPoint() {
            return new Point(1000000, 1000000);
        }

        @Override
        public Rect getBounds() {
            Point p = getPoint();
            return new Rect(p.x - 190, p.y - 190, p.x + 190, p.y + 190);
        }
    };

    Point getPoint();

    Rect getBounds();
}
