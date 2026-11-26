@file:Suppress("DEPRECATION") // Deliberately uses the 1.x-style setters to prove they still compile.

package uk.co.deanwild.materialshowcaseview

import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MaterialShowcaseSequenceTest {

    private val screen = TestScreen()
    private val shown = mutableListOf<Int>()
    private val dismissed = mutableListOf<Int>()

    @Before
    fun reset() = MaterialShowcaseView.resetAll(screen.activity)

    private fun sequence(id: String? = null): MaterialShowcaseSequence {
        val seq = if (id == null) MaterialShowcaseSequence(screen.activity)
        else MaterialShowcaseSequence(screen.activity, id)
        seq.setOnItemShownListener { _, position -> shown += position }
        seq.setOnItemDismissedListener { _, position -> dismissed += position }
        for ((i, target) in listOf(screen.top, screen.middle, screen.bottom).withIndex()) {
            val item = MaterialShowcaseView.Builder(screen.activity)
                .setTarget(target)
                .setContentText("item $i")
                .setDismissText("NEXT")
                .setSkipText("SKIP")
                .build()
            item.setAnimationFactory(CountingAnimationFactory())
            seq.addSequenceItem(item)
        }
        return seq
    }

    private fun current(): MaterialShowcaseView = screen.showcases().single()

    private fun currentText() = current().findViewById<TextView>(R.id.tv_content).text.toString()

    private fun dismissCurrent() {
        current().hide()
        idle()
    }

    @Test
    fun showsItemsInOrder() {
        sequence().start()
        idle()
        assertThat(currentText()).isEqualTo("item 0")
        dismissCurrent()
        assertThat(currentText()).isEqualTo("item 1")
        dismissCurrent()
        assertThat(currentText()).isEqualTo("item 2")
        dismissCurrent()
        assertThat(screen.showcases()).isEmpty()
    }

    // Bug 9: positions only advanced when single-use was enabled
    @Test
    fun listenerPositions_advance_withoutSingleUse() {
        sequence().start()
        idle()
        repeat(3) { dismissCurrent() }

        assertThat(shown).containsExactly(0, 1, 2).inOrder()
        assertThat(dismissed).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun listenerPositions_advance_withSingleUse() {
        sequence("positions").start()
        idle()
        repeat(3) { dismissCurrent() }

        assertThat(shown).containsExactly(0, 1, 2).inOrder()
        assertThat(dismissed).containsExactly(0, 1, 2).inOrder()
    }

    @Test
    fun singleUse_finishedSequence_doesNotShowAgain() {
        val seq = sequence("finished")
        seq.start()
        idle()
        repeat(3) { dismissCurrent() }
        assertThat(seq.hasFired()).isTrue()

        sequence("finished").start()
        idle()
        assertThat(screen.showcases()).isEmpty()
    }

    @Test
    fun singleUse_resumesWhereItLeftOff() {
        sequence("resume").start()
        idle()
        dismissCurrent()
        // The user leaves mid-sequence (e.g. rotation) while item 1 is showing.
        screen.decor.removeView(current())

        shown.clear()
        sequence("resume").start()
        idle()

        assertThat(currentText()).isEqualTo("item 1")
        assertThat(shown).containsExactly(1)
    }

    // Bug 10: a finishing Activity marked the whole sequence as finished
    @Test
    fun activityFinishingMidSequence_doesNotMarkSequenceFinished() {
        val seq = sequence("finishing")
        seq.start()
        idle()

        screen.activity.finish()
        dismissCurrent()

        assertThat(seq.hasFired()).isFalse()
    }

    @Test
    fun skip_endsSequence_andMarksItFinished() {
        val seq = sequence("skip")
        seq.start()
        idle()

        current().skip()
        idle()

        assertThat(screen.showcases()).isEmpty()
        assertThat(seq.hasFired()).isTrue()
        assertThat(dismissed).containsExactly(0)
    }

    @Test
    fun hasFired_withoutSingleUse_isFalse() {
        assertThat(sequence().hasFired()).isFalse()
    }

    @Test
    fun emptySequence_startIsNoOp() {
        MaterialShowcaseSequence(screen.activity).start()
        idle()
        assertThat(screen.showcases()).isEmpty()
    }

    // Bug 12 (sequence side): config overwrote colours set on the item
    @Test
    fun config_doesNotOverrideItemColours_itDidNotSet() {
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setConfig(ShowcaseConfig().apply { setDelay(0) })
        val item = MaterialShowcaseView.Builder(screen.activity)
            .setTarget(screen.middle)
            .setContentText("x")
            .setContentTextColor(0xFFFF0000.toInt())
            .setDismissTextColor(0xFF0000FF.toInt())
            .build()

        seq.addSequenceItem(item)

        assertThat(item.findViewById<TextView>(R.id.tv_content).currentTextColor).isEqualTo(0xFFFF0000.toInt())
        assertThat(item.findViewById<TextView>(R.id.tv_dismiss).currentTextColor).isEqualTo(0xFF0000FF.toInt())
    }

    @Test
    fun config_appliesToConvenienceItems() {
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setConfig(ShowcaseConfig().apply { setContentTextColor(0xFF00FF00.toInt()) })
        seq.addSequenceItem(screen.middle, "title", "content", "GOT IT")
        seq.start()
        idle()

        assertThat(current().findViewById<TextView>(R.id.tv_content).currentTextColor).isEqualTo(0xFF00FF00.toInt())
    }
}
