package uk.co.deanwild.materialshowcaseview

import android.app.AlertDialog
import android.view.View
import android.widget.Button
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TransitionsAndDialogsTest {

    private val screen = TestScreen()

    private fun item(target: View, factory: CountingAnimationFactory = CountingAnimationFactory()) =
        MaterialShowcaseView.Builder(screen.activity).setTarget(target).setContentText("x").setDismissText("OK")
            .build().apply { setAnimationFactory(factory) }

    private fun MaterialShowcaseView.holeInWindow(): android.graphics.Point {
        val loc = IntArray(2).also { getLocationInWindow(it) }
        val hole = currentHolePosition()
        return android.graphics.Point(loc[0] + hole.x, loc[1] + hole.y)
    }

    // #12 and #192: the next item covers the screen straight away, starting from the previous target
    @Test
    fun dismissingAnItem_handsStraightOverToTheNext() {
        val first = item(screen.top)
        val second = item(screen.bottom)
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.addSequenceItem(first)
        seq.addSequenceItem(second)
        seq.start()
        idleFor(1000)
        val firstHole = first.holeInWindow()

        first.hide()

        // Both overlays are up and the new one is already visible: no uncovered gap.
        assertThat(screen.showcases()).containsExactly(first, second)
        assertThat(second.visibility).isEqualTo(View.VISIBLE)
        // The glide starts from the previous highlight. (Robolectric's frame clock completes
        // animators on their first frame, so the in-between motion is checked on a device.)
        assertThat(second.holeInWindow()).isEqualTo(firstHole)

        idleFor(1000) // glide finished, old overlay removed
        assertThat(screen.showcases()).containsExactly(second)
        assertThat(second.holeInWindow()).isEqualTo(screen.bottom.windowCentre())
    }

    @Test
    fun handOver_doesNotReplayTheRevealAnimation_andReportsDisplayedAtTheEnd() {
        val secondFactory = CountingAnimationFactory()
        val second = item(screen.bottom, secondFactory)
        val displayed = RecordingListener()
        second.addShowcaseListener(displayed)
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.addSequenceItem(item(screen.top))
        seq.addSequenceItem(second)
        seq.start()
        idleFor(1000)

        screen.showcases().single().hide()
        assertThat(displayed.displayed).isEmpty() // reported once the glide ends, not on hand-over
        idleFor(1000)

        assertThat(secondFactory.animateInCount).isEqualTo(0)
        assertThat(displayed.displayed).containsExactly(second)
    }

    @Test
    fun animateBetweenItemsOff_restoresOneByOneBehaviour() {
        val secondFactory = CountingAnimationFactory()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setAnimateBetweenItems(false)
        seq.addSequenceItem(item(screen.top))
        seq.addSequenceItem(item(screen.bottom, secondFactory))
        seq.start()
        idleFor(1000)

        screen.showcases().single().hide()
        idleFor(1000)

        assertThat(secondFactory.animateInCount).isEqualTo(1)
    }

    @Test
    fun lastItem_stillAnimatesOut() {
        val lastFactory = CountingAnimationFactory()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.addSequenceItem(item(screen.top))
        seq.addSequenceItem(item(screen.bottom, lastFactory))
        seq.start()
        idleFor(1000)
        screen.showcases().single().hide()
        idleFor(1000)

        screen.showcases().single().hide()
        idleFor(1000)

        assertThat(lastFactory.animateOutCount).isEqualTo(1)
        assertThat(screen.showcases()).isEmpty()
    }

    @Test
    fun dismissingMidGlide_continuesFromWhereTheHighlightIs() {
        val seq = MaterialShowcaseSequence(screen.activity)
        repeat(3) { seq.addSequenceItem(item(listOf(screen.top, screen.middle, screen.bottom)[it])) }
        seq.start()
        idleFor(1000)
        screen.showcases().single().hide()
        idleFor(100) // part-way through the glide

        val second = screen.showcases().single()
        second.hide()
        idleFor(1000)

        assertThat(screen.showcases().single().holeInWindow()).isEqualTo(screen.bottom.windowCentre())
    }

    // #51: showing over a dialog
    @Test
    fun showOverDialog_coversTheDialog_andLinesUpWithItsViews() {
        val button = Button(screen.activity).apply { text = "Delete" }
        val dialog = AlertDialog.Builder(screen.activity).setView(button).create()
        dialog.show()
        idle()

        val view = item(button)
        assertThat(view.show(dialog)).isTrue()
        idleFor(1000)

        assertThat(view.isAttachedToWindow).isTrue()
        assertThat(view.rootView).isNotSameInstanceAs(dialog.window!!.decorView)
        assertThat(view.rootView).isNotSameInstanceAs(screen.decor)
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isTrue()

        val viewOnScreen = IntArray(2).also { view.getLocationOnScreen(it) }
        val buttonOnScreen = IntArray(2).also { button.getLocationOnScreen(it) }
        val hole = view.currentHolePosition()
        assertThat(viewOnScreen[0] + hole.x).isEqualTo(buttonOnScreen[0] + button.width / 2)
        assertThat(viewOnScreen[1] + hole.y).isEqualTo(buttonOnScreen[1] + button.height / 2)
    }

    @Test
    fun dismissingTheDialog_removesTheShowcase() {
        val button = Button(screen.activity)
        val dialog = AlertDialog.Builder(screen.activity).setView(button).create()
        dialog.show()
        idle()
        val listener = RecordingListener()
        val view = item(button).apply { addShowcaseListener(listener) }
        view.show(dialog)
        idleFor(1000)

        dialog.dismiss()
        idle()

        assertThat(view.isAttachedToWindow).isFalse()
        assertThat(listener.dismissed).containsExactly(view)
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isFalse()
    }

    @Test
    fun hidingOverADialog_leavesTheDialogUp() {
        val button = Button(screen.activity)
        val dialog = AlertDialog.Builder(screen.activity).setView(button).create()
        dialog.show()
        idle()
        val view = item(button)
        view.show(dialog)
        idleFor(1000)

        view.hide()
        idle()

        assertThat(view.isAttachedToWindow).isFalse()
        assertThat(dialog.isShowing).isTrue()
    }

    @Test
    fun showOverADialogThatIsNotShowing_failsClearly() {
        val dialog = AlertDialog.Builder(screen.activity).setMessage("x").create()
        val error = assertThrows(IllegalStateException::class.java) { item(screen.middle).show(dialog) }
        assertThat(error.message).contains("Show the dialog")
    }

    @Test
    fun sequenceOverADialog_stepsThroughItsItems() {
        val one = Button(screen.activity)
        val two = Button(screen.activity)
        val column = android.widget.LinearLayout(screen.activity).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            addView(one)
            addView(two)
        }
        val dialog = AlertDialog.Builder(screen.activity).setView(column).create()
        dialog.show()
        idle()
        val shown = mutableListOf<Int>()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setDialog(dialog)
        seq.setOnItemShownListener { _, pos -> shown += pos }
        val first = item(one)
        seq.addSequenceItem(first)
        seq.addSequenceItem(item(two))
        seq.start()
        idleFor(1000)

        first.hide()
        idleFor(1000)

        assertThat(shown).containsExactly(0, 1).inOrder()
        assertThat(first.isAttachedToWindow).isFalse()
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isTrue()
    }
}
