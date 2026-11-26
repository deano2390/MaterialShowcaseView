package uk.co.deanwild.materialshowcaseview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.graphics.Point
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import kotlin.math.hypot
import kotlin.math.max

/** Calls [block] exactly once when the animation ends *or* is cancelled. */
internal fun Animator.doOnEndOrCancel(block: () -> Unit) {
    addListener(object : AnimatorListenerAdapter() {
        private var done = false
        override fun onAnimationEnd(animation: Animator) = finish()
        override fun onAnimationCancel(animation: Animator) = finish()
        private fun finish() {
            if (!done) {
                done = true
                block()
            }
        }
    })
}

internal fun Animator.doOnStart(block: () -> Unit) {
    addListener(object : AnimatorListenerAdapter() {
        override fun onAnimationStart(animation: Animator) = block()
    })
}

/** Radius needed for a circle centred on [point] to cover the whole of [view]. */
internal fun coveringRadius(view: View, point: Point): Float {
    val dx = max(point.x, view.width - point.x).toFloat()
    val dy = max(point.y, view.height - point.y).toFloat()
    return hypot(dx, dy)
}

/** Animates the showcase's highlight from its current position to [point]. */
internal fun animateShowcaseToPoint(showcaseView: MaterialShowcaseView, point: Point) {
    val startX = showcaseView.showcaseX
    val startY = showcaseView.showcaseY
    ValueAnimator.ofFloat(0f, 1f).apply {
        interpolator = AccelerateDecelerateInterpolator()
        addUpdateListener {
            val f = it.animatedFraction
            showcaseView.setPosition(
                (startX + (point.x - startX) * f).toInt(),
                (startY + (point.y - startY) * f).toInt(),
            )
        }
        start()
    }
}
