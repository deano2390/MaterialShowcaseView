package uk.co.deanwild.materialshowcaseview

import android.graphics.Point
import android.view.View
import android.view.ViewAnimationUtils

/** Reveals the showcase as a circle expanding from (and collapsing into) the target. */
open class CircularRevealAnimationFactory : IAnimationFactory {

    override fun animateInView(
        target: View,
        point: Point,
        duration: Long,
        listener: IAnimationFactory.AnimationStartListener,
    ) {
        if (!target.isAttachedToWindow) {
            listener.onAnimationStart()
            return
        }
        ViewAnimationUtils.createCircularReveal(target, point.x, point.y, 0f, coveringRadius(target, point)).apply {
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
        // Animating a detached view throws "Cannot start this animator on a detached view!"
        if (!target.isAttachedToWindow) {
            listener.onAnimationEnd()
            return
        }
        ViewAnimationUtils.createCircularReveal(target, point.x, point.y, coveringRadius(target, point), 0f).apply {
            this.duration = duration
            // A cancelled animation (e.g. the window going away) must still finish the dismissal.
            doOnEndOrCancel { listener.onAnimationEnd() }
            start()
        }
    }

    override fun animateTargetToPoint(showcaseView: MaterialShowcaseView, point: Point) =
        animateShowcaseToPoint(showcaseView, point)
}
