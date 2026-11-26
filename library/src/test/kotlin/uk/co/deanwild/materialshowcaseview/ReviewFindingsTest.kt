package uk.co.deanwild.materialshowcaseview

import android.graphics.Point
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

/** Regression tests for issues found in an independent review of the 2.0 rewrite. */
@RunWith(AndroidJUnit4::class)
class ReviewFindingsTest {

    private fun builder(screen: TestScreen, target: View? = screen.middle) =
        MaterialShowcaseView.Builder(screen.activity).setTarget(target).setContentText("x").setDismissText("OK")

    private fun MaterialShowcaseView.withFactory(f: IAnimationFactory = CountingAnimationFactory()) =
        apply { setAnimationFactory(f) }

    // Finding 2: the overlay's inset padding was applied to the tooltip twice
    @Test
    fun tooltipPosition_ignoresOverlayPadding() {
        fun gapBelowTarget(topInset: Int): Int {
            val screen = TestScreen()
            val tooltip = ShowcaseTooltip.build(screen.activity).text("Tip")
            val view = builder(screen, screen.top).setToolTip(tooltip).build().withFactory()
            view.show(screen.activity)
            view.applyInsets(
                WindowInsetsCompat.Builder()
                    .setInsets(WindowInsetsCompat.Type.statusBars(), Insets.of(0, topInset, 0, 0)).build(),
            )
            idleFor(1000)
            val tip = (0 until view.childCount).map { view.getChildAt(it) }
                .filterIsInstance<ShowcaseTooltip.TooltipView>().single()
            val tipLoc = IntArray(2).also { tip.getLocationInWindow(it) }
            val targetLoc = IntArray(2).also { screen.top.getLocationInWindow(it) }
            return tipLoc[1] - (targetLoc[1] + screen.top.height)
        }
        assertThat(gapBelowTarget(100)).isEqualTo(gapBelowTarget(0))
    }

    @Test
    fun tooltip_isNotShownBeforeTheShowcaseIsDisplayed() {
        val screen = TestScreen()
        val view = builder(screen).setToolTip(ShowcaseTooltip.build(screen.activity).text("Tip"))
            .setDelay(5000).build().withFactory()
        view.show(screen.activity)
        idleFor(1000)
        assertThat((0 until view.childCount).map { view.getChildAt(it) }
            .filterIsInstance<ShowcaseTooltip.TooltipView>()).isEmpty()
    }

    // Finding 3: a sequence stalled forever on an item whose show() returned false
    @Test
    fun sequence_skipsItemsThatCannotShow() {
        val screen = TestScreen()
        MaterialShowcaseView.resetAll(screen.activity)
        PrefsManager(screen.activity, "already-seen").setFired()
        val shown = mutableListOf<Int>()
        val seq = MaterialShowcaseSequence(screen.activity, "skips-unshowable")
        seq.setOnItemShownListener { _, pos -> shown += pos }
        seq.addSequenceItem(builder(screen, screen.top).build().withFactory())
        seq.addSequenceItem(builder(screen, screen.middle).singleUse("already-seen").build().withFactory())
        seq.addSequenceItem(builder(screen, screen.bottom).build().withFactory())

        seq.start()
        idle()
        screen.showcases().single().hide()
        idle()
        screen.showcases().single().hide()
        idle()

        assertThat(shown).containsExactly(0, 2).inOrder()
        assertThat(screen.showcases()).isEmpty()
        assertThat(seq.hasFired()).isTrue()
    }

    // Finding 4: overlapping showcases removed first-in-first-out left the app hidden from TalkBack
    @Test
    fun overlappingShowcases_restoreAccessibility_inAnyOrder() {
        val screen = TestScreen()
        val content = screen.decor.getChildAt(0)
        val original = content.importantForAccessibility

        val a = builder(screen).build().withFactory()
        a.show(screen.activity)
        val b = builder(screen, screen.top).build().withFactory()
        b.show(screen.activity)
        idle()

        screen.decor.removeView(a)
        assertThat(content.importantForAccessibility).isEqualTo(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS)
        screen.decor.removeView(b)
        assertThat(content.importantForAccessibility).isEqualTo(original)
    }

    // Finding 5: a tooltip without a ViewTarget threw after the showcase was already half-shown
    @Test
    fun tooltipWithoutViewTarget_failsBeforeAnySideEffects() {
        val screen = TestScreen()
        MaterialShowcaseView.resetAll(screen.activity)
        val content = screen.decor.getChildAt(0)
        val original = content.importantForAccessibility
        val view = builder(screen, target = null).setToolTip(ShowcaseTooltip.build(screen.activity))
            .singleUse("tooltip-no-target").build().withFactory()

        assertThrows(IllegalStateException::class.java) { view.show(screen.activity) }

        assertThat(screen.showcases()).isEmpty()
        assertThat(content.importantForAccessibility).isEqualTo(original)
        assertThat(PrefsManager(screen.activity, "tooltip-no-target").hasFired()).isFalse()
    }

    // Finding 6: an item finishing its exit animation as the Activity is destroyed (e.g. rotation)
    // advanced the sequence onto the dead Activity
    @Test
    fun sequence_doesNotAdvance_whenActivityIsDestroyed() {
        val screen = TestScreen()
        val deferred = DeferredExitFactory()
        val shown = mutableListOf<Int>()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setOnItemShownListener { _, pos -> shown += pos }
        seq.addSequenceItem(builder(screen, screen.top).build().withFactory(deferred))
        seq.addSequenceItem(builder(screen, screen.bottom).build().withFactory(deferred))
        seq.start()
        idle()

        screen.showcases().single().hide() // exit animation starts...
        screen.controller.pause().stop().destroy() // ...and the Activity goes away mid-animation
        idle()

        assertThat(shown).containsExactly(0)
    }

    /** Exit animations never finish on their own, like a real animation cut short by teardown. */
    private class DeferredExitFactory : IAnimationFactory {
        override fun animateInView(target: View, point: Point, duration: Long, listener: IAnimationFactory.AnimationStartListener) =
            listener.onAnimationStart()

        override fun animateOutView(target: View, point: Point, duration: Long, listener: IAnimationFactory.AnimationEndListener) = Unit

        override fun animateTargetToPoint(showcaseView: MaterialShowcaseView, point: Point) = Unit
    }
}
