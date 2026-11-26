package uk.co.deanwild.materialshowcaseview

import android.graphics.Point
import android.view.View

/** Animates a showcase in and out. [Point]s are in the showcase view's own coordinates. */
interface IAnimationFactory {

    fun animateInView(target: View, point: Point, duration: Long, listener: AnimationStartListener)

    fun animateOutView(target: View, point: Point, duration: Long, listener: AnimationEndListener)

    fun animateTargetToPoint(showcaseView: MaterialShowcaseView, point: Point)

    fun interface AnimationStartListener {
        fun onAnimationStart()
    }

    fun interface AnimationEndListener {
        fun onAnimationEnd()
    }
}
