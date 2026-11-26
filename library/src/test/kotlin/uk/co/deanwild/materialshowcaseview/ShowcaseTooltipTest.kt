package uk.co.deanwild.materialshowcaseview

import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ShowcaseTooltipTest {

    private val screen = TestScreen()

    // Bug 17: customView(int) dereferenced the target view before it was configured
    @Test
    fun customViewById_beforeTargetConfigured_doesNotCrash() {
        val custom = TextView(screen.activity).apply { id = android.R.id.custom }
        screen.root.addView(custom)

        ShowcaseTooltip.build(screen.activity).customView(android.R.id.custom)
    }

    @Test
    fun tooltip_isShownAndRemovedWithShowcase() {
        val tooltip = ShowcaseTooltip.build(screen.activity)
            .text("Hello tooltip")
            .color(0xFF000000.toInt())
            .textColor(0xFFFFFFFF.toInt())
            .corner(10)

        val view = MaterialShowcaseView.Builder(screen.activity)
            .setTarget(screen.middle)
            .setToolTip(tooltip)
            .build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(screen.activity)
        idleFor(1000)

        assertThat(tooltipViews(view)).hasSize(1)

        view.hide()
        idleFor(1000)
        assertThat(screen.showcases()).isEmpty()
    }

    private fun tooltipViews(root: android.view.ViewGroup) =
        (0 until root.childCount).map { root.getChildAt(it) }.filterIsInstance<ShowcaseTooltip.TooltipView>()
}
