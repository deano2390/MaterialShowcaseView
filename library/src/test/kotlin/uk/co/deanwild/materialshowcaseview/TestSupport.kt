package uk.co.deanwild.materialshowcaseview

import android.app.Activity
import android.graphics.Point
import android.os.Looper
import android.os.SystemClock
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import org.robolectric.Robolectric
import org.robolectric.Shadows.shadowOf
import java.time.Duration

/** Runs everything currently due on the main looper. */
fun idle() = shadowOf(Looper.getMainLooper()).idle()

/** Advances the fake clock and runs everything that becomes due. */
fun idleFor(millis: Long) = shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(millis))

/**
 * A resumed, laid-out Activity with three buttons:
 * [top] near the top-left, [middle] in the centre and [bottom] near the bottom.
 */
class TestScreen {
    val controller = Robolectric.buildActivity(Activity::class.java).setup()
    val activity: Activity = controller.get()
    val root = FrameLayout(activity)
    val top = button("top", Gravity.TOP or Gravity.START)
    val middle = button("middle", Gravity.CENTER)
    val bottom = button("bottom", Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)

    init {
        activity.setContentView(root)
        idle()
    }

    val decor: ViewGroup get() = activity.window.decorView as ViewGroup

    /** All MaterialShowcaseViews currently attached to the decor view. */
    fun showcases(): List<MaterialShowcaseView> =
        (0 until decor.childCount).map { decor.getChildAt(it) }.filterIsInstance<MaterialShowcaseView>()

    private fun button(label: String, gravity: Int): Button {
        val b = Button(activity)
        b.text = label
        b.id = View.generateViewId()
        root.addView(b, FrameLayout.LayoutParams(200, 100, gravity))
        return b
    }
}

/**
 * Animation factory that completes animations synchronously and counts calls,
 * so tests don't depend on Robolectric's animator emulation.
 */
class CountingAnimationFactory : IAnimationFactory {
    var animateInCount = 0
    var animateOutCount = 0

    override fun animateInView(target: View, point: Point, duration: Long, listener: IAnimationFactory.AnimationStartListener) {
        animateInCount++
        listener.onAnimationStart()
    }

    override fun animateOutView(target: View, point: Point, duration: Long, listener: IAnimationFactory.AnimationEndListener) {
        animateOutCount++
        listener.onAnimationEnd()
    }

    override fun animateTargetToPoint(showcaseView: MaterialShowcaseView, point: Point) {}
}

class RecordingListener : IShowcaseListener {
    val displayed = mutableListOf<MaterialShowcaseView>()
    val dismissed = mutableListOf<MaterialShowcaseView>()
    override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView) { displayed += showcaseView }
    override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView) { dismissed += showcaseView }
}

fun motion(action: Int, x: Float, y: Float): MotionEvent {
    val now = SystemClock.uptimeMillis()
    return MotionEvent.obtain(now, now, action, x, y, 0)
}

/** Centre of [view] in window coordinates. */
fun View.windowCentre(): Point {
    val loc = IntArray(2)
    getLocationInWindow(loc)
    return Point(loc[0] + width / 2, loc[1] + height / 2)
}
