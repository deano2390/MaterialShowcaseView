@file:Suppress("DEPRECATION") // Deliberately uses the 1.x-style setters to prove they still compile.

package uk.co.deanwild.materialshowcaseview

import android.graphics.Point
import android.graphics.Rect
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RuntimeEnvironment
import uk.co.deanwild.materialshowcaseview.shape.CircleShape
import uk.co.deanwild.materialshowcaseview.target.Target
import uk.co.deanwild.materialshowcaseview.target.ViewTarget
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

@RunWith(AndroidJUnit4::class)
class MaterialShowcaseViewTest {

    private val screen = TestScreen()
    private val factory = CountingAnimationFactory()
    private val listener = RecordingListener()

    private fun builder(target: View? = screen.middle) = MaterialShowcaseView.Builder(screen.activity).apply {
        if (target != null) setTarget(target)
        setContentText("content")
        setDismissText("GOT IT")
        setListener(listener)
    }

    private fun MaterialShowcaseView.Builder.showWithFactory(): MaterialShowcaseView {
        val view = build()
        view.setAnimationFactory(factory)
        view.show(screen.activity)
        idle()
        return view
    }

    // --- basic lifecycle -------------------------------------------------------------------

    @Test
    fun show_attachesToDecor_andNotifiesDisplayed() {
        val view = builder().showWithFactory()

        assertThat(screen.showcases()).containsExactly(view)
        assertThat(view.visibility).isEqualTo(View.VISIBLE)
        assertThat(listener.displayed).containsExactly(view)
        assertThat(factory.animateInCount).isEqualTo(1)
    }

    @Test
    fun hide_removesFromDecor_andNotifiesDismissedOnce() {
        val view = builder().showWithFactory()

        view.hide()
        idle()

        assertThat(screen.showcases()).isEmpty()
        assertThat(listener.dismissed).containsExactly(view)
    }

    @Test
    fun showDelay_isRespected() {
        val view = builder().setDelay(1000).build()
        view.setAnimationFactory(factory)
        view.show(screen.activity)

        idleFor(500)
        assertThat(listener.displayed).isEmpty()

        idleFor(600)
        assertThat(listener.displayed).containsExactly(view)
    }

    // --- Bug 1: hide() runs on every MotionEvent and is not idempotent ---------------------

    @Test
    fun dismissOnTouch_fullGesture_animatesOutExactlyOnce() {
        val view = builder().setDismissOnTouch(true).showWithFactory()

        view.onTouch(view, motion(MotionEvent.ACTION_DOWN, 5f, 5f))
        view.onTouch(view, motion(MotionEvent.ACTION_MOVE, 6f, 6f))
        view.onTouch(view, motion(MotionEvent.ACTION_UP, 6f, 6f))
        idle()

        assertThat(factory.animateOutCount).isEqualTo(1)
        assertThat(listener.dismissed).hasSize(1)
    }

    @Test
    fun hide_calledTwice_doesNotCrash() {
        val view = builder().showWithFactory()

        view.hide()
        idle()
        view.hide() // e.g. a second queued click on "GOT IT"
        idle()

        assertThat(factory.animateOutCount).isEqualTo(1)
        assertThat(listener.dismissed).hasSize(1)
    }

    @Test
    fun dismissButton_doubleClick_dismissesOnce() {
        val view = builder().showWithFactory()
        val dismiss = view.findViewById<View>(R.id.tv_dismiss)

        dismiss.performClick()
        dismiss.performClick()
        idle()

        assertThat(factory.animateOutCount).isEqualTo(1)
        assertThat(listener.dismissed).hasSize(1)
    }

    // --- Bug 2: animating out a detached view ----------------------------------------------

    @Test
    fun hide_whenNeverAttached_doesNotStartAnimation() {
        val view = builder().build()
        view.setAnimationFactory(factory)

        view.hide()
        idle()

        assertThat(factory.animateOutCount).isEqualTo(0)
    }

    @Test
    fun removedBeforeDelayElapses_neverDisplays() {
        val view = builder().setDelay(1000).build()
        view.setAnimationFactory(factory)
        view.show(screen.activity)

        view.removeFromWindow()
        idleFor(2000)

        assertThat(factory.animateInCount).isEqualTo(0)
        assertThat(view.visibility).isNotEqualTo(View.VISIBLE)
    }

    // --- Bug 3: global-layout listener leaks onto the Activity's ViewTreeObserver ----------

    @Test
    fun afterDismiss_layoutPassesNoLongerQueryTheTarget() {
        val target = CountingTarget(screen.middle)
        val view = builder(target = null).build()
        view.setTarget(target)
        view.setAnimationFactory(factory)
        view.show(screen.activity)
        idle()
        view.hide()
        idle()

        target.calls = 0
        screen.decor.viewTreeObserver.dispatchOnGlobalLayout()

        assertThat(target.calls).isEqualTo(0)
    }

    // --- Bug 5: navigation bar handled via hidden resource instead of insets ---------------

    @Test
    fun noSystemBarInsets_meansNoNavBarMargin() {
        val view = builder().showWithFactory()
        val lp = view.layoutParams as FrameLayout.LayoutParams

        // Robolectric reports no navigation bar, so nothing should be reserved for one.
        assertThat(lp.bottomMargin).isEqualTo(0)
    }

    // --- Bug 6: ViewTarget mixes laid-out width with measured width ------------------------

    @Test
    fun viewTarget_boundsMatchLaidOutSize() {
        val v = screen.middle
        // Simulate a parent that lays the child out at a different size than it measured.
        v.measure(
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(300, View.MeasureSpec.EXACTLY),
        )
        v.layout(v.left, v.top, v.left + 50, v.top + 40)

        val bounds = ViewTarget(v).getBounds()
        assertThat(bounds.width()).isEqualTo(50)
        assertThat(bounds.height()).isEqualTo(40)
        assertThat(ViewTarget(v).getPoint()).isEqualTo(Point(bounds.centerX(), bounds.centerY()))
    }

    // --- Bug 7: no target ------------------------------------------------------------------

    @Test
    fun build_withoutTarget_doesNotCrash() {
        val view = builder(target = null).showWithFactory()
        assertThat(screen.showcases()).containsExactly(view)
    }

    @Test
    fun build_withoutTarget_rectangleShape_doesNotCrash() {
        val view = builder(target = null).withRectangleShape().showWithFactory()
        assertThat(screen.showcases()).containsExactly(view)
    }

    @Test
    fun targetTouchable_withoutTarget_touchDoesNotCrash() {
        val view = builder(target = null).setTargetTouchable(true).showWithFactory()
        view.onTouch(view, motion(MotionEvent.ACTION_DOWN, 5f, 5f))
    }

    @Test
    fun targetTouchable_touchOnTarget_passesThroughAndDismisses() {
        val view = builder().setTargetTouchable(true).showWithFactory()
        val c = screen.middle.windowCentre()

        val consumed = view.onTouch(view, motion(MotionEvent.ACTION_DOWN, c.x.toFloat(), c.y.toFloat()))
        idle()

        assertThat(consumed).isFalse()
        assertThat(listener.dismissed).hasSize(1)
    }

    @Test
    fun touchOutsideTarget_isConsumed_andDoesNotDismissByDefault() {
        val view = builder().setTargetTouchable(true).showWithFactory()

        val consumed = view.onTouch(view, motion(MotionEvent.ACTION_DOWN, 1f, 1f))
        view.onTouch(view, motion(MotionEvent.ACTION_UP, 1f, 1f))
        idle()

        assertThat(consumed).isTrue()
        assertThat(listener.dismissed).isEmpty()
    }

    // --- Bug 8: skip() on a single-use showcase resets the "already shown" flag ------------

    @Test
    fun singleUse_skip_isRemembered() {
        val view = builder().setSkipText("SKIP").singleUse("skip-test").showWithFactory()

        view.skip()
        idle()

        val again = builder().singleUse("skip-test").build()
        assertThat(again.show(screen.activity)).isFalse()
    }

    @Test
    fun singleUse_dismiss_isRemembered() {
        val view = builder().singleUse("dismiss-test").showWithFactory()
        view.hide()
        idle()

        assertThat(builder().singleUse("dismiss-test").build().show(screen.activity)).isFalse()
    }

    @Test
    fun singleUse_unexpectedDetach_showsAgainNextTime() {
        val view = builder().singleUse("detach-test").showWithFactory()

        // e.g. rotation or the user backing out of the Activity
        screen.decor.removeView(view)

        assertThat(builder().singleUse("detach-test").build().show(screen.activity)).isTrue()
    }

    // --- Bug 13: title text --------------------------------------------------------------

    @Test
    fun titleText_doesNotFadeContentText() {
        val view = builder().setTitleText("Title").build()
        assertThat(view.findViewById<TextView>(R.id.tv_content).alpha).isEqualTo(1f)
    }

    @Test
    fun noTitle_hidesTitleView() {
        val view = builder().build()
        assertThat(view.findViewById<View>(R.id.tv_title).visibility).isEqualTo(View.GONE)
    }

    @Test
    fun emptyTitle_hidesTitleView() {
        val view = builder().setTitleText("").build()
        assertThat(view.findViewById<View>(R.id.tv_title).visibility).isEqualTo(View.GONE)
    }

    @Test
    fun nullTitle_doesNotCrash() {
        val view = builder().setTitleText(null as CharSequence?).build()
        assertThat(view.findViewById<View>(R.id.tv_title).visibility).isEqualTo(View.GONE)
    }

    @Test
    fun title_isShownWhenSet() {
        val view = builder().setTitleText("Title").build()
        val title = view.findViewById<TextView>(R.id.tv_title)
        assertThat(title.visibility).isEqualTo(View.VISIBLE)
        assertThat(title.text.toString()).isEqualTo("Title")
    }

    // --- Bug 14: hasFired() without singleUse ----------------------------------------------

    @Test
    fun hasFired_withoutSingleUse_isFalse() {
        assertThat(builder().build().hasFired()).isFalse()
    }

    // --- Bug 16: text sizes ignore the user's font scale -----------------------------------

    @Test
    fun textSizes_followFontScale() {
        val normal = builder().build().findViewById<TextView>(R.id.tv_content).textSize

        RuntimeEnvironment.setFontScale(2f)
        val scaled = TestScreen().let { s ->
            MaterialShowcaseView.Builder(s.activity).setTarget(s.middle).setContentText("x").build()
        }.findViewById<TextView>(R.id.tv_content).textSize

        // Android 14+ scales large text non-linearly, so expect growth rather than exactly 2x.
        assertThat(scaled).isGreaterThan(normal * 1.5f)
    }

    // --- Bug 18: show() off the main thread ------------------------------------------------

    @Test
    fun show_offMainThread_failsFastWithClearMessage() {
        val view = builder().build()
        val executor = Executors.newSingleThreadExecutor()
        val error = executor.submit<Throwable?> {
            try {
                view.show(screen.activity); null
            } catch (t: Throwable) {
                t
            }
        }.get(5, TimeUnit.SECONDS)
        executor.shutdown()

        assertThat(error).isInstanceOf(IllegalStateException::class.java)
        assertThat(error!!.message).contains("main thread")
    }

    // --- Bug 19: accessibility -------------------------------------------------------------

    @Test
    fun whileShown_underlyingContentIsHiddenFromAccessibility_andRestoredAfter() {
        val content = screen.decor.getChildAt(0)
        val before = content.importantForAccessibility

        val view = builder().showWithFactory()
        assertThat(content.importantForAccessibility)
            .isEqualTo(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)

        view.hide()
        idle()
        assertThat(content.importantForAccessibility).isEqualTo(before)
    }

    // --- Content placement -----------------------------------------------------------------

    @Test
    fun targetInTopHalf_contentSitsBelowTarget() {
        val view = builder(screen.top).showWithFactory()
        val lp = view.findViewById<View>(R.id.content_box).layoutParams as FrameLayout.LayoutParams
        assertThat(lp.gravity and Gravity.VERTICAL_GRAVITY_MASK).isEqualTo(Gravity.TOP)
        assertThat(lp.topMargin).isGreaterThan(screen.top.windowCentre().y)
    }

    @Test
    fun targetInBottomHalf_contentSitsAboveTarget() {
        val view = builder(screen.bottom).showWithFactory()
        val lp = view.findViewById<View>(R.id.content_box).layoutParams as FrameLayout.LayoutParams
        assertThat(lp.gravity and Gravity.VERTICAL_GRAVITY_MASK).isEqualTo(Gravity.BOTTOM)
        assertThat(lp.bottomMargin).isGreaterThan(view.height - screen.bottom.windowCentre().y)
    }

    /** The dismiss button is laid out completely inside the content box, and the box is on screen. */
    private fun assertDismissFullyVisible(view: MaterialShowcaseView) {
        val box = view.findViewById<View>(R.id.content_box)
        val dismiss = view.findViewById<View>(R.id.tv_dismiss)
        assertThat(dismiss.height).isGreaterThan(0)
        assertThat(dismiss.bottom).isAtMost(box.height)
        assertThat(box.top).isAtLeast(view.paddingTop)
        assertThat(box.bottom).isAtMost(view.height - view.paddingBottom)
    }

    private fun longText(lines: Int) = List(lines) { "This line explains the feature in some detail." }.joinToString(" ")

    // Issue #171: content that didn't fit below/above the target pushed the dismiss button off screen
    @Test
    fun tallContent_targetInTopHalf_dismissStaysVisible() {
        val target = android.widget.Button(screen.activity)
        screen.root.addView(target, FrameLayout.LayoutParams(100, 50, Gravity.TOP or Gravity.START).apply { topMargin = 180 })
        idle()

        val view = builder(target).setTitleText("Title").setContentText(longText(4)).showWithFactory()
        idle()

        assertDismissFullyVisible(view)
    }

    @Test
    fun tallContent_targetInBottomHalf_dismissStaysVisible() {
        val target = android.widget.Button(screen.activity)
        screen.root.addView(target, FrameLayout.LayoutParams(100, 50, Gravity.BOTTOM or Gravity.START).apply { bottomMargin = 180 })
        idle()

        val view = builder(target).setTitleText("Title").setContentText(longText(4)).showWithFactory()
        idle()

        assertDismissFullyVisible(view)
    }

    @Test
    fun veryTallContent_dismissStaysVisible() {
        val view = builder(screen.middle).setTitleText("Title").setContentText(longText(6)).showWithFactory()
        idle()

        assertDismissFullyVisible(view)
    }

    @Test
    fun customGravity_isRespected() {
        val view = builder().setGravity(Gravity.CENTER).showWithFactory()
        val lp = view.findViewById<View>(R.id.content_box).layoutParams as FrameLayout.LayoutParams
        assertThat(lp.gravity).isEqualTo(Gravity.CENTER)
    }

    @Test
    fun emptyDismissText_hidesDismissButton() {
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle).build()
        assertThat(view.findViewById<View>(R.id.tv_dismiss).visibility).isEqualTo(View.GONE)
    }

    @Test
    fun skipButton_hiddenUnlessSkipTextSet() {
        val without = builder().build()
        assertThat(without.findViewById<View>(R.id.tv_skip).visibility).isEqualTo(View.GONE)

        val with = builder().setSkipText("SKIP").build()
        assertThat(with.findViewById<View>(R.id.tv_skip).visibility).isEqualTo(View.VISIBLE)
    }

    // --- Bug 12 (view side): config only overrides what it sets ----------------------------

    @Test
    fun setConfig_doesNotOverrideColoursItDidNotSet() {
        val view = builder().setContentTextColor(0xFFFF0000.toInt()).build()

        view.setConfig(ShowcaseConfig().apply { setDelay(10) })

        assertThat(view.findViewById<TextView>(R.id.tv_content).currentTextColor).isEqualTo(0xFFFF0000.toInt())
    }

    @Test
    fun setConfig_appliesColoursItDidSet() {
        val view = builder().setContentTextColor(0xFFFF0000.toInt()).build()

        view.setConfig(ShowcaseConfig().apply { setContentTextColor(0xFF00FF00.toInt()) })

        assertThat(view.findViewById<TextView>(R.id.tv_content).currentTextColor).isEqualTo(0xFF00FF00.toInt())
    }

    @Test
    fun setConfig_shapePadding_isAppliedToShape() {
        val shape = CircleShape(50)
        val view = builder().build()

        view.setConfig(ShowcaseConfig().apply {
            setShape(shape)
            setShapePadding(30)
        })

        assertThat(shape.getTotalRadius()).isEqualTo(80)
    }

    @Test
    fun builderShapePadding_isAppliedToShape() {
        val shape = CircleShape(50)
        builder().setShape(shape).setShapePadding(25).build()
        assertThat(shape.getTotalRadius()).isEqualTo(75)
    }

    private class CountingTarget(view: View) : Target {
        private val delegate = ViewTarget(view)
        var calls = 0
        override fun getPoint(): Point { calls++; return delegate.getPoint() }
        override fun getBounds(): Rect { calls++; return delegate.getBounds() }
    }
}
