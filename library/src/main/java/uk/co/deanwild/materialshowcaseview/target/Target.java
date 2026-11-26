package uk.co.deanwild.materialshowcaseview.target;

import android.graphics.Point;
import android.graphics.Rect;

/**
 * Something to showcase. Coordinates are in the window's coordinate space.
 * <p>
 * Declared in Java on purpose, so Kotlin implementations written against 1.x keep compiling.
 */
public interface Target {

    /** No target: the showcase masks the whole screen and centres its content. */
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

    /** Centre of the target, in window coordinates. */
    Point getPoint();

    /** Bounds of the target, in window coordinates. */
    Rect getBounds();
}
