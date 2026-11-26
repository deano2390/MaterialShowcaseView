package uk.co.deanwild.materialshowcaseview;

import android.graphics.Point;
import android.view.View;

/**
 * Animates a showcase in and out. Points are in the showcase view's own coordinates.
 * <p>
 * Declared in Java on purpose; see {@link IShowcaseListener}.
 */
public interface IAnimationFactory {

    void animateInView(View target, Point point, long duration, AnimationStartListener listener);

    void animateOutView(View target, Point point, long duration, AnimationEndListener listener);

    void animateTargetToPoint(MaterialShowcaseView showcaseView, Point point);

    interface AnimationStartListener {
        void onAnimationStart();
    }

    interface AnimationEndListener {
        void onAnimationEnd();
    }
}
