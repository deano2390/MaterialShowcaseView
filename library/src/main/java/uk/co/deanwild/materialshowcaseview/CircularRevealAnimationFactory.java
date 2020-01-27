package uk.co.deanwild.materialshowcaseview;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.TargetApi;
import android.graphics.Point;
import android.os.Build;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.animation.AccelerateDecelerateInterpolator;


public class CircularRevealAnimationFactory implements IAnimationFactory {

    private static final String ALPHA = "alpha";
    private static final float INVISIBLE = 0f;
    private static final float VISIBLE = 1f;

    private final AccelerateDecelerateInterpolator interpolator;

    public CircularRevealAnimationFactory() {
        interpolator = new AccelerateDecelerateInterpolator();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void animateInView(View target, Point point, long duration, final AnimationStartListener listener) {
        Animator animator = ViewAnimationUtils.createCircularReveal(target, point.x, point.y, 0,
                target.getWidth() > target.getHeight() ? target.getWidth() : target.getHeight());
        animator.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animation) {

            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void animateOutView(View target, Point point, long duration, final AnimationEndListener listener) {
        Animator animator = ViewAnimationUtils.createCircularReveal(target, point.x, point.y,
                target.getWidth() > target.getHeight() ? target.getWidth() : target.getHeight(), 0);
        animator.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });

        animator.start();
    }

    @Override
    public void animateTargetToPoint(MaterialShowcaseView showcaseView, Point point) {
        AnimatorSet set = new AnimatorSet();
        ObjectAnimator xAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseX", point.x);
        ObjectAnimator yAnimator = ObjectAnimator.ofInt(showcaseView, "showcaseY", point.y);
        set.playTogether(xAnimator, yAnimator);
        set.setInterpolator(interpolator);
        set.start();
    }
}
