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


public class FadeAnimationFactory implements IAnimationFactory{

    private static final String ALPHA = "alpha";
    private static final float INVISIBLE = 0f;
    private static final float VISIBLE = 1f;

    private final AccelerateDecelerateInterpolator interpolator;

    public FadeAnimationFactory() {
        interpolator = new AccelerateDecelerateInterpolator();
    }

    @Override
    public void animateInView(View target, Point point, long duration, final AnimationStartListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE, VISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
                listener.onAnimationStart();
            }

            @Override
            public void onAnimationEnd(Animator animator) {
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        oa.start();
    }

    @Override
    public void animateOutView(View target, Point point, long duration, final AnimationEndListener listener) {
        ObjectAnimator oa = ObjectAnimator.ofFloat(target, ALPHA, INVISIBLE);
        oa.setDuration(duration).addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {
            }

            @Override
            public void onAnimationEnd(Animator animator) {
                listener.onAnimationEnd();
            }

            @Override
            public void onAnimationCancel(Animator animator) {
            }

            @Override
            public void onAnimationRepeat(Animator animator) {
            }
        });
        oa.start();
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
