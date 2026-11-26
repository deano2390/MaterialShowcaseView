package uk.co.deanwild.materialshowcaseview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.RippleDrawable
import android.widget.TextView
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import uk.co.deanwild.materialshowcaseview.shape.CircleShape

@RunWith(AndroidJUnit4::class)
class ButtonsAndHelpersTest {

    private val screen = TestScreen()

    @After
    fun reEnable() = MaterialShowcaseView.setShowcasesEnabled(true)

    private fun builder() = MaterialShowcaseView.Builder(screen.activity)
        .setTarget(screen.middle).setContentText("x").setDismissText("GOT IT").setSkipText("SKIP")

    private fun MaterialShowcaseView.shown() = apply {
        setAnimationFactory(CountingAnimationFactory())
        show(screen.activity)
        idle()
    }

    private fun dp(v: Int) = (v * screen.activity.resources.displayMetrics.density).toInt()

    // #174, PR #188: buttons instead of plain text
    @Test
    fun buttonColour_givesAFilledButton() {
        val view = builder().setDismissButtonColor(Color.BLUE).setSkipButtonColor(Color.GRAY).build()
        val dismiss = view.findViewById<TextView>(R.id.tv_dismiss)
        val skip = view.findViewById<TextView>(R.id.tv_skip)

        assertThat(dismiss.background).isInstanceOf(RippleDrawable::class.java)
        assertThat(skip.background).isInstanceOf(RippleDrawable::class.java)
        assertThat(dismiss.paddingStart).isEqualTo(dp(16))
    }

    @Test
    fun customBackground_isUsed_andNullRestoresPlainText() {
        val custom = ColorDrawable(Color.RED)
        val view = builder().setDismissBackground(custom).build()
        val dismiss = view.findViewById<TextView>(R.id.tv_dismiss)
        assertThat(dismiss.background).isSameInstanceAs(custom)

        val plain = builder().setDismissBackground(custom).setDismissBackground(null).build()
        val plainDismiss = plain.findViewById<TextView>(R.id.tv_dismiss)
        assertThat(plainDismiss.background).isNotNull()
        assertThat(plainDismiss.background).isNotSameInstanceAs(custom)
        assertThat(plainDismiss.paddingStart).isEqualTo(dp(5))
    }

    @Test
    fun config_appliesButtonColours() {
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setConfig(ShowcaseConfig().apply { dismissButtonColor = Color.BLUE })
        seq.addSequenceItem(screen.middle, "content", "GOT IT")
        seq.start()
        idle()

        val dismiss = screen.showcases().single().findViewById<TextView>(R.id.tv_dismiss)
        assertThat(dismiss.background).isInstanceOf(RippleDrawable::class.java)
    }

    // #61
    @Test
    fun isShowing_tracksTheOverlay() {
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isFalse()
        val view = builder().build().shown()
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isTrue()
        view.hide()
        idle()
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isFalse()
    }

    @Test
    fun isShowing_isTrueThroughoutASequence() {
        val seq = MaterialShowcaseSequence(screen.activity)
        repeat(2) { seq.addSequenceItem(builder().build().apply { setAnimationFactory(CountingAnimationFactory()) }) }
        seq.start()
        idle()
        screen.showcases().single().hide()
        idle()
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isTrue()
        screen.showcases().single().hide()
        idle()
        assertThat(MaterialShowcaseView.isShowing(screen.activity)).isFalse()
    }

    // #104
    @Test
    fun disabled_showsNothing_andRecordsNothing() {
        MaterialShowcaseView.resetAll(screen.activity)
        MaterialShowcaseView.setShowcasesEnabled(false)

        val shown = builder().singleUse("disabled-single").build().apply { setAnimationFactory(CountingAnimationFactory()) }
            .show(screen.activity)
        val seq = MaterialShowcaseSequence(screen.activity, "disabled-seq")
        seq.addSequenceItem(screen.middle, "content", "GOT IT")
        seq.start()
        idle()

        assertThat(shown).isFalse()
        assertThat(screen.showcases()).isEmpty()
        assertThat(MaterialShowcaseView.hasFired(screen.activity, "disabled-single")).isFalse()
        assertThat(seq.hasFired()).isFalse()

        MaterialShowcaseView.setShowcasesEnabled(true)
        assertThat(builder().singleUse("disabled-single").build().shown().parent).isNotNull()
    }

    @Test
    fun disabledMidSequence_stopsWithoutFinishing() {
        val seq = MaterialShowcaseSequence(screen.activity, "disabled-mid")
        MaterialShowcaseView.resetAll(screen.activity)
        repeat(2) { seq.addSequenceItem(builder().build().apply { setAnimationFactory(CountingAnimationFactory()) }) }
        seq.start()
        idle()

        MaterialShowcaseView.setShowcasesEnabled(false)
        screen.showcases().single().hide()
        idle()

        assertThat(screen.showcases()).isEmpty()
        assertThat(seq.hasFired()).isFalse()
    }

    // #96
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun shapeBorder_outlinesTheHole() {
        val view = builder().setShape(CircleShape(60).apply { isAdjustToTarget = false })
            .setShapePadding(0).setShapeBorder(Color.RED, 8).build().shown()

        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        val loc = IntArray(2).also { view.getLocationInWindow(it) }
        val c = screen.middle.windowCentre()
        val cx = c.x - loc[0]
        val cy = c.y - loc[1]

        assertThat(bitmap.getPixel(cx, cy - 60)).isEqualTo(Color.RED) // on the edge
        assertThat(Color.alpha(bitmap.getPixel(cx, cy))).isEqualTo(0) // still a hole inside
    }
}
