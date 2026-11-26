package uk.co.deanwild.materialshowcaseviewsample

import android.app.Activity
import android.content.pm.ActivityInfo
import android.os.SystemClock
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.hamcrest.Matchers.containsString
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView
import uk.co.deanwild.materialshowcaseview.ShowcaseTooltip

@RunWith(AndroidJUnit4::class)
class SampleShowcaseTest {

    @Before
    fun resetFlags() {
        MaterialShowcaseView.resetAll(InstrumentationRegistry.getInstrumentation().targetContext)
    }

    private fun <A : Activity> ActivityScenario<A>.showcases(): List<MaterialShowcaseView> {
        var result = emptyList<MaterialShowcaseView>()
        onActivity { activity ->
            val decor = activity.window.decorView as ViewGroup
            result = (0 until decor.childCount).map { decor.getChildAt(it) }
                .filterIsInstance<MaterialShowcaseView>()
                .filter { it.isShown }
        }
        return result
    }

    /** Waits (the samples use show delays) until [predicate] holds, failing after [timeoutMs]. */
    private fun <A : Activity> ActivityScenario<A>.waitUntil(
        timeoutMs: Long = 5000,
        message: String,
        predicate: (List<MaterialShowcaseView>) -> Boolean,
    ) {
        val end = SystemClock.uptimeMillis() + timeoutMs
        while (SystemClock.uptimeMillis() < end) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            if (predicate(showcases())) return
            SystemClock.sleep(50)
        }
        throw AssertionError("Timed out: $message")
    }

    private fun <A : Activity> ActivityScenario<A>.awaitShowcase() =
        waitUntil(message = "showcase to appear") { it.isNotEmpty() }

    private fun <A : Activity> ActivityScenario<A>.awaitNoShowcase() =
        waitUntil(message = "showcase to disappear") { it.isEmpty() }

    private fun <A : Activity> ActivityScenario<A>.currentContent(): String {
        var text = ""
        onActivity { text = showcases().single().findViewById<TextView>(uk.co.deanwild.materialshowcaseview.R.id.tv_content).text.toString() }
        return text
    }

    @Test
    fun simpleExample_showsAndDismisses() {
        ActivityScenario.launch(SimpleSingleExample::class.java).use { scenario ->
            scenario.awaitShowcase()
            onView(withText("GOT IT")).perform(click())
            scenario.awaitNoShowcase()
        }
    }

    @Test
    fun simpleExample_isSingleUse() {
        ActivityScenario.launch(SimpleSingleExample::class.java).use { scenario ->
            scenario.awaitShowcase()
            onView(withText("GOT IT")).perform(click())
            scenario.awaitNoShowcase()
        }
        ActivityScenario.launch(SimpleSingleExample::class.java).use { scenario ->
            SystemClock.sleep(2000)
            assertTrue(scenario.showcases().isEmpty())
        }
    }

    @Test
    fun customExample_dismissOnTouchAnywhere() {
        ActivityScenario.launch(CustomExample::class.java).use { scenario ->
            scenario.awaitShowcase()
            onView(withText("This is some amazing feature you should know about")).perform(click())
            scenario.awaitNoShowcase()
        }
    }

    @Test
    fun sequenceExample_stepsThroughEveryItem() {
        ActivityScenario.launch(SequenceExample::class.java).use { scenario ->
            for (expected in listOf("This is button one", "This is button two", "This is button three")) {
                scenario.waitUntil(message = "item '$expected'") { list ->
                    list.size == 1 && list.single()
                        .findViewById<TextView>(uk.co.deanwild.materialshowcaseview.R.id.tv_content).text.toString() == expected
                }
                onView(withText("GOT IT")).perform(click())
            }
            scenario.awaitNoShowcase()
        }
    }

    @Test
    fun sequenceExample_skipEndsSequence() {
        ActivityScenario.launch(SequenceExample::class.java).use { scenario ->
            scenario.awaitShowcase()
            onView(withText("GOT IT")).perform(click())
            scenario.waitUntil(message = "second item") { it.size == 1 && scenario.currentContent() == "This is button two" }
            onView(withText("SKIP")).perform(click())
            scenario.awaitNoShowcase()
            SystemClock.sleep(1500)
            assertTrue(scenario.showcases().isEmpty())
        }
    }

    @Test
    fun tooltipExample_showsTooltipAndDismissesOnTouch() {
        fun hasTooltip(list: List<MaterialShowcaseView>) = list.size == 1 && list.single().let { v ->
            (0 until v.childCount).any { v.getChildAt(it) is ShowcaseTooltip.TooltipView }
        }
        ActivityScenario.launch(TooltipExample::class.java).use { scenario ->
            scenario.waitUntil(message = "first tooltip") { hasTooltip(it) }
            onView(withText(containsString("Tap anywhere to continue"))).perform(click())
            scenario.waitUntil(message = "second tooltip") { list ->
                hasTooltip(list) && scenario.tooltipText().contains("another")
            }
            onView(withText(containsString("another"))).perform(click())
            scenario.awaitNoShowcase()
        }
    }

    private fun <A : Activity> ActivityScenario<A>.tooltipText(): String {
        var text = ""
        onActivity {
            val tooltip = showcases().singleOrNull()?.let { v ->
                (0 until v.childCount).map { v.getChildAt(it) }.filterIsInstance<ShowcaseTooltip.TooltipView>().firstOrNull()
            }
            text = ((tooltip?.getChildAt(0)) as? TextView)?.text?.toString() ?: ""
        }
        return text
    }

    @Test
    fun rotationMidShowcase_doesNotCrash_andShowsAgain() {
        ActivityScenario.launch(SimpleSingleExample::class.java).use { scenario ->
            scenario.awaitShowcase()
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE }
            SystemClock.sleep(1500)
            // Not dismissed, so the single-use flag was reset and it shows again after recreation.
            scenario.awaitShowcase()
            assertEquals(1, scenario.showcases().size)
            scenario.onActivity { it.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT }
        }
    }
}
