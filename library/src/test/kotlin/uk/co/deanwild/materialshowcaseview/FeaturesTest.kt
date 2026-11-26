package uk.co.deanwild.materialshowcaseview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.util.TypedValue
import android.view.Gravity
import android.view.KeyEvent
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.annotation.GraphicsMode
import uk.co.deanwild.materialshowcaseview.shape.RoundedRectangleShape

/** Features added in 2.0 from long-standing requests and PRs. */
@RunWith(AndroidJUnit4::class)
class FeaturesTest {

    private val screen = TestScreen()

    private fun builder() = MaterialShowcaseView.Builder(screen.activity)
        .setTarget(screen.middle).setTitleText("Title").setContentText("x").setDismissText("OK").setSkipText("SKIP")

    private fun MaterialShowcaseView.shown() = apply {
        setAnimationFactory(CountingAnimationFactory())
        show(screen.activity)
        idle()
    }

    private fun px(sp: Float) =
        TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, screen.activity.resources.displayMetrics)

    // PR #196, issues #84 #151 #194 #113
    @Test
    fun textSizesAndTypefaces_canBeSet() {
        val view = builder()
            .setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            .setContentTextSize(TypedValue.COMPLEX_UNIT_SP, 13f)
            .setDismissTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
            .setSkipTextSize(TypedValue.COMPLEX_UNIT_SP, 15f)
            .setTitleStyle(Typeface.DEFAULT_BOLD)
            .setContentStyle(Typeface.MONOSPACE)
            .setSkipTextColor(Color.RED)
            .build()

        assertThat(view.findViewById<TextView>(R.id.tv_title).textSize).isWithin(0.5f).of(px(12f))
        assertThat(view.findViewById<TextView>(R.id.tv_content).textSize).isWithin(0.5f).of(px(13f))
        assertThat(view.findViewById<TextView>(R.id.tv_dismiss).textSize).isWithin(0.5f).of(px(14f))
        assertThat(view.findViewById<TextView>(R.id.tv_skip).textSize).isWithin(0.5f).of(px(15f))
        assertThat(view.findViewById<TextView>(R.id.tv_title).typeface).isEqualTo(Typeface.DEFAULT_BOLD)
        assertThat(view.findViewById<TextView>(R.id.tv_content).typeface).isEqualTo(Typeface.MONOSPACE)
        assertThat(view.findViewById<TextView>(R.id.tv_skip).currentTextColor).isEqualTo(Color.RED)
    }

    @Test
    fun config_appliesTextSizesAndTypefaces() {
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setConfig(ShowcaseConfig().apply {
            setContentTextSize(TypedValue.COMPLEX_UNIT_SP, 11f)
            setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
            contentTextStyle = Typeface.SERIF
        })
        seq.addSequenceItem(screen.middle, "Title", "content", "OK")
        seq.start()
        idle()

        val item = screen.showcases().single()
        assertThat(item.findViewById<TextView>(R.id.tv_content).textSize).isWithin(0.5f).of(px(11f))
        assertThat(item.findViewById<TextView>(R.id.tv_title).textSize).isWithin(0.5f).of(px(16f))
        assertThat(item.findViewById<TextView>(R.id.tv_content).typeface).isEqualTo(Typeface.SERIF)
    }

    // PRs #204 #87, issues #60 #106 #107
    @Test
    fun sequence_reportsSkipAndFinish() {
        val skipped = mutableListOf<Int>()
        val finished = mutableListOf<Boolean>()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setOnSkippedListener { _, pos -> skipped += pos }
        seq.setOnSequenceFinishedListener { wasSkipped -> finished += wasSkipped }
        repeat(3) { seq.addSequenceItem(builder().build().apply { setAnimationFactory(CountingAnimationFactory()) }) }

        seq.start()
        idle()
        screen.showcases().single().hide()
        idle()
        screen.showcases().single().skip()
        idle()

        assertThat(skipped).containsExactly(1)
        assertThat(finished).containsExactly(true)
    }

    @Test
    fun sequence_reportsFinishWhenCompleted() {
        val finished = mutableListOf<Boolean>()
        val seq = MaterialShowcaseSequence(screen.activity)
        seq.setOnSequenceFinishedListener { finished += it }
        repeat(2) { seq.addSequenceItem(builder().build().apply { setAnimationFactory(CountingAnimationFactory()) }) }

        seq.start()
        idle()
        screen.showcases().single().hide()
        idle()
        assertThat(finished).isEmpty()
        screen.showcases().single().hide()
        idle()

        assertThat(finished).containsExactly(false)
    }

    @Test
    fun wasSkipped_tellsSkipFromDismiss() {
        val reasons = mutableListOf<Boolean>()
        val listener = object : IShowcaseListener {
            override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView) = Unit
            override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView) {
                reasons += showcaseView.wasSkipped()
            }
        }
        builder().setListener(listener).build().shown().skip()
        idle()
        builder().setListener(listener).build().shown().hide()
        idle()

        assertThat(reasons).containsExactly(true, false).inOrder()
    }

    // PR #204, issue #159
    @Test
    @GraphicsMode(GraphicsMode.Mode.NATIVE)
    fun roundedRectangle_hasRoundedCorners() {
        val shape = RoundedRectangleShape(Rect(0, 0, 100, 60), false, 20)
        assertThat(shape.getWidth()).isEqualTo(100)
        assertThat(shape.getHeight()).isEqualTo(60)
        shape.setPadding(5)
        assertThat(shape.getTotalRadius()).isEqualTo(35)

        val bitmap = Bitmap.createBitmap(200, 200, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.BLACK) }
        val eraser = Paint(Paint.ANTI_ALIAS_FLAG).apply { xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR) }
        shape.draw(Canvas(bitmap), eraser, 100, 100)
        // Centre is cleared; the very corner of the (padded) rectangle is not, because it's rounded.
        assertThat(Color.alpha(bitmap.getPixel(100, 100))).isEqualTo(0)
        assertThat(Color.alpha(bitmap.getPixel(100 - 55 + 1, 100 - 35 + 1))).isEqualTo(255)
    }

    @Test
    fun builder_withRoundedRectangleShape_usesIt() {
        val view = builder().withRoundedRectangleShape(16).build().shown()
        assertThat(screen.showcases()).containsExactly(view)
    }

    // Issues #24 #93
    @Test
    fun staticHasFired_reportsSingleUseState() {
        MaterialShowcaseView.resetAll(screen.activity)
        assertThat(MaterialShowcaseView.hasFired(screen.activity, "static-check")).isFalse()

        builder().singleUse("static-check").build().shown().hide()
        idle()

        assertThat(MaterialShowcaseView.hasFired(screen.activity, "static-check")).isTrue()
    }

    // Issue #149
    @Test
    fun highlight_followsTheTarget_whenContentScrolls() {
        val scroll = ScrollView(screen.activity)
        val column = FrameLayout(screen.activity).apply { minimumHeight = 3000 }
        val target = Button(screen.activity)
        column.addView(target, FrameLayout.LayoutParams(100, 50, Gravity.TOP).apply { topMargin = 300 })
        scroll.addView(column, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 3000))
        screen.root.addView(scroll, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        idle()

        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(target).build().shown()
        val before = view.showcaseY

        scroll.scrollBy(0, 100)
        // On a device the next draw pass dispatches this; Robolectric doesn't draw, so do it here.
        android.view.ViewTreeObserver::class.java.getDeclaredMethod("dispatchOnScrollChanged")
            .apply { isAccessible = true }
            .invoke(screen.decor.viewTreeObserver)

        assertThat(scroll.scrollY).isEqualTo(100)
        assertThat(target.windowCentre().y - IntArray(2).also { view.getLocationInWindow(it) }[1]).isEqualTo(before - 100)
        assertThat(view.showcaseY).isEqualTo(before - 100)
    }

    // Issue #88
    @Test
    fun backPress_skips_withOnBackPressedDispatcher() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val view = MaterialShowcaseView.Builder(activity).setContentText("x").setDismissOnBackPress(true).build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(activity)
        idle()

        activity.onBackPressedDispatcher.onBackPressed()
        idle()

        assertThat(view.parent).isNull()
        assertThat(view.wasSkipped()).isTrue()
        assertThat(activity.isFinishing).isFalse()
    }

    @Test
    fun backPress_skips_onPlainActivity() {
        val view = builder().setDismissOnBackPress(true).build().shown()

        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_BACK))
        view.dispatchKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_BACK))
        idle()

        assertThat(screen.showcases()).isEmpty()
        assertThat(view.wasSkipped()).isTrue()
    }

    @Test
    fun backPress_isIgnored_byDefault() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val view = MaterialShowcaseView.Builder(activity).setContentText("x").build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(activity)
        idle()

        assertThat(activity.onBackPressedDispatcher.hasEnabledCallbacks()).isFalse()
    }

    @Test
    fun backCallback_isRemoved_whenShowcaseGoesAway() {
        val activity = Robolectric.buildActivity(ComponentActivity::class.java).setup().get()
        val view = MaterialShowcaseView.Builder(activity).setContentText("x").setDismissOnBackPress(true).build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(activity)
        idle()
        view.hide()
        idle()

        assertThat(activity.onBackPressedDispatcher.hasEnabledCallbacks()).isFalse()
    }
}
