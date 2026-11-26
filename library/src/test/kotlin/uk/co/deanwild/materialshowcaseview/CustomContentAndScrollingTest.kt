package uk.co.deanwild.materialshowcaseview

import android.graphics.Rect
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CustomContentAndScrollingTest {

    private val screen = TestScreen()

    private fun MaterialShowcaseView.shown() = apply {
        setAnimationFactory(CountingAnimationFactory())
        show(screen.activity)
        idle()
    }

    private fun customContent(): LinearLayout = LinearLayout(screen.activity).apply {
        orientation = LinearLayout.VERTICAL
        addView(TextView(context).apply { text = "Custom explanation" })
        addView(Button(context).apply { id = R.id.showcase_dismiss; text = "Next" })
        addView(Button(context).apply { id = R.id.showcase_skip; text = "Don't show again" })
    }

    // #11: custom content
    @Test
    fun customContent_replacesTheBuiltInBlock() {
        val content = customContent()
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.top)
            .setContentText("built-in text").setContentView(content).build().shown()

        assertThat(content.parent).isSameInstanceAs(view)
        assertThat(view.findViewById<View>(R.id.content_box)).isNull()
        // Still positioned below a target in the top half.
        assertThat(content.top).isGreaterThan(screen.top.windowCentre().y)
    }

    @Test
    fun customContent_dismissAndSkipIdsAreWired() {
        val listener = RecordingListener()
        val a = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle)
            .setContentView(customContent()).setListener(listener).build().shown()
        a.findViewById<View>(R.id.showcase_dismiss).performClick()
        idle()
        val b = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle)
            .setContentView(customContent()).setListener(listener).build().shown()
        b.findViewById<View>(R.id.showcase_skip).performClick()
        idle()

        assertThat(listener.dismissed).containsExactly(a, b).inOrder()
        assertThat(a.wasSkipped()).isFalse()
        assertThat(b.wasSkipped()).isTrue()
    }

    @Test
    fun customContent_fromLayoutResource() {
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle)
            .setContentLayout(android.R.layout.simple_list_item_1).build()
        assertThat(view.findViewById<View>(android.R.id.text1)).isNotNull()
        assertThat(view.findViewById<View>(R.id.content_box)).isNull()
    }

    @Test
    fun customContent_nullRestoresTheBuiltInBlock() {
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle)
            .setContentView(customContent()).setContentView(null).setContentText("back").build()
        assertThat(view.findViewById<TextView>(R.id.tv_content).text.toString()).isEqualTo("back")
        assertThat(view.findViewById<View>(R.id.showcase_dismiss)).isNull()
    }

    @Test
    fun customContent_tallContentStaysOnScreen() {
        val tall = LinearLayout(screen.activity).apply {
            orientation = LinearLayout.VERTICAL
            repeat(6) { addView(View(context), LinearLayout.LayoutParams(10, 40)) }
            addView(Button(context).apply { id = R.id.showcase_dismiss })
        }
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle).setContentView(tall)
            .build().shown()
        idle()

        val dismiss = view.findViewById<View>(R.id.showcase_dismiss)
        assertThat(dismiss.height).isGreaterThan(0)
        assertThat(dismiss.bottom).isAtMost(tall.height)
        assertThat(tall.bottom).isAtMost(view.height - view.paddingBottom)
    }

    @Test
    fun customContent_inASequence_handsOver() {
        val seq = MaterialShowcaseSequence(screen.activity)
        val first = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.top).setContentView(customContent()).build()
            .apply { setAnimationFactory(CountingAnimationFactory()) }
        seq.addSequenceItem(first)
        seq.addSequenceItem(screen.bottom, "second", "OK")
        seq.start()
        idleFor(1000)

        first.findViewById<View>(R.id.showcase_dismiss).performClick()
        idleFor(1000)

        val second = screen.showcases().single()
        assertThat(second.findViewById<TextView>(R.id.tv_content).text.toString()).isEqualTo("second")
    }

    // #6: scroll targets into view
    private fun scrollingScreen(): Pair<ScrollView, Button> {
        val scroll = ScrollView(screen.activity)
        val column = FrameLayout(screen.activity).apply { minimumHeight = 3000 }
        val target = Button(screen.activity)
        column.addView(target, FrameLayout.LayoutParams(100, 50, Gravity.TOP).apply { topMargin = 2000 })
        scroll.addView(column)
        screen.root.addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        idle()
        return scroll to target
    }

    @Test
    fun scrollTargetIntoView_bringsAnOffScreenTargetOnScreen() {
        val (scroll, target) = scrollingScreen()
        assertThat(target.getGlobalVisibleRect(Rect())).isFalse()

        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(target)
            .setScrollTargetIntoView(true).build().shown()

        assertThat(scroll.scrollY).isGreaterThan(0)
        val visible = Rect()
        assertThat(target.getGlobalVisibleRect(visible)).isTrue()
        assertThat(visible.height()).isEqualTo(target.height)
        assertThat(view.currentHolePosition().y).isIn(com.google.common.collect.Range.closed(0, view.height))
    }

    @Test
    fun scrollTargetIntoView_isOffByDefault() {
        val (scroll, target) = scrollingScreen()
        MaterialShowcaseView.Builder(screen.activity).setTarget(target).build().shown()
        assertThat(scroll.scrollY).isEqualTo(0)
    }

    @Test
    fun scrollTargetIntoView_viaConfig_forSequenceItems() {
        val (scroll, target) = scrollingScreen()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setConfig(ShowcaseConfig().apply { scrollTargetIntoView = true })
        seq.addSequenceItem(screen.top, "first", "OK")
        seq.addSequenceItem(target, "second", "OK")
        seq.start()
        idleFor(1000)
        assertThat(scroll.scrollY).isEqualTo(0)

        screen.showcases().single().hide()
        idleFor(1000)

        assertThat(scroll.scrollY).isGreaterThan(0)
    }
}
