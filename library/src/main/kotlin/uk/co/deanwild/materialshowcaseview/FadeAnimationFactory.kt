package uk.co.deanwild.materialshowcaseview

import android.animation.ObjectAnimator
import android.graphics.Point
import android.view.View

/** Fades the showcase in and out. */
open class FadeAnimationFactory : IAnimationFactory {

    override fun animateInView(
        target: View,
        point: Point,
        duration: Long,
        listener: IAnimationFactory.AnimationStartListener,
    ) {
        ObjectAnimator.ofFloat(target, View.ALPHA, 0f, 1f).apply {
            this.duration = duration
            doOnStart { listener.onAnimationStart() }
            start()
        }
    }

    override fun animateOutView(
        target: View,
        point: Point,
        duration: Long,
        listener: IAnimationFactory.AnimationEndListener,
    ) {
        ObjectAnimator.ofFloat(target, View.ALPHA, 0f).apply {
            this.duration = duration
            doOnEndOrCancel { listener.onAnimationEnd() }
            start()
        }
    }

    override fun animateTargetToPoint(showcaseView: MaterialShowcaseView, point: Point) =
        animateShowcaseToPoint(showcaseView, point)
}
