package uk.co.deanwild.materialshowcaseview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Point
import android.view.View
import androidx.core.graphics.Insets
import androidx.core.view.WindowInsetsCompat
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode
import uk.co.deanwild.materialshowcaseview.shape.CircleShape

/** Behaviour that relies on APIs introduced in 2.0, so it has no 1.x equivalent. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class NewBehaviourTest {

    private val screen = TestScreen()
    private val mask = 0xDD335075.toInt()

    private fun show(builder: MaterialShowcaseView.Builder = MaterialShowcaseView.Builder(screen.activity)): MaterialShowcaseView {
        val view = builder.setTarget(screen.middle).setMaskColour(mask).setContentText("x").build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(screen.activity)
        idle()
        return view
    }

    /** 3-button navigation: the whole navigation bar is tappable. */
    private fun navBarInsets(bottom: Int) = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottom))
        .setInsets(WindowInsetsCompat.Type.tappableElement(), Insets.of(0, 0, 0, bottom))
        .build()

    /** Gesture navigation: a navigation bar inset, but nothing tappable. */
    private fun gestureNavInsets(bottom: Int) = WindowInsetsCompat.Builder()
        .setInsets(WindowInsetsCompat.Type.navigationBars(), Insets.of(0, 0, 0, bottom))
        .build()

    private fun render(view: View) = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        .also { view.draw(Canvas(it)) }

    // Bug 5: content is kept clear of the system bars
    @Test
    fun systemBarInsets_padTheContent() {
        val view = show()
        view.applyInsets(navBarInsets(120))
        assertThat(view.paddingBottom).isEqualTo(120)
    }

    // Bug 5: by default the navigation bar isn't dimmed
    @Test
    fun navigationBar_isNotMasked_byDefault() {
        val view = show()
        view.applyInsets(navBarInsets(120))
        idle()

        val bitmap = render(view)
        assertThat(Color.alpha(bitmap.getPixel(2, view.height - 10))).isEqualTo(0)
        assertThat(bitmap.getPixel(2, view.height - 130)).isEqualTo(mask)
    }

    @Test
    fun gestureNavigation_handleAreaIsMasked() {
        val view = show()
        view.applyInsets(gestureNavInsets(48))
        idle()

        val bitmap = render(view)
        assertThat(bitmap.getPixel(2, view.height - 10)).isEqualTo(mask)
        // ...but the content still stays clear of the gesture area.
        assertThat(view.paddingBottom).isEqualTo(48)
    }

    @Test
    fun navigationBar_isMasked_whenRenderingOverIt() {
        val view = show(MaterialShowcaseView.Builder(screen.activity).renderOverNavigationBar())
        view.applyInsets(navBarInsets(120))
        idle()

        assertThat(render(view).getPixel(2, view.height - 10)).isEqualTo(mask)
    }

    // Bug 20: animateTargetToPoint animated properties that didn't exist
    @Test
    fun animateTargetToPoint_movesTheHighlight() {
        val view = show()

        FadeAnimationFactory().animateTargetToPoint(view, Point(40, 60))
        idleFor(1000)

        assertThat(view.showcaseX).isEqualTo(40)
        assertThat(view.showcaseY).isEqualTo(60)
    }

    // Bug 15
    @Test
    fun removeShowcaseListener_removesIt() {
        val listener = RecordingListener()
        val view = MaterialShowcaseView.Builder(screen.activity).setTarget(screen.middle).setListener(listener).build()
        view.setAnimationFactory(CountingAnimationFactory())

        view.removeShowcaseListener(listener)
        view.show(screen.activity)
        idle()

        assertThat(listener.displayed).isEmpty()
    }

    @Test
    fun show_whileAlreadyShowing_returnsFalse() {
        val view = show()
        assertThat(view.show(screen.activity)).isFalse()
        assertThat(screen.showcases()).hasSize(1)
    }

    @Test
    fun view_canBeShownAgainAfterDismissal() {
        val view = show()
        view.hide()
        idle()

        assertThat(view.show(screen.activity)).isTrue()
        idle()
        view.hide()
        idle()
        assertThat(screen.showcases()).isEmpty()
    }

    @Test
    fun constructedView_withoutBuilder_canShowAndHide() {
        val view = MaterialShowcaseView(screen.activity)
        view.setShape(CircleShape(10))
        view.setAnimationFactory(CountingAnimationFactory())

        assertThat(view.show(screen.activity)).isTrue()
        idle()
        view.hide()
        idle()
        assertThat(screen.showcases()).isEmpty()
    }

    @Test
    fun noTarget_centresContentAndMasksEverything() {
        val view = MaterialShowcaseView.Builder(screen.activity).setMaskColour(mask).setContentText("x").build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(screen.activity)
        idle()

        val c = screen.middle.windowCentre()
        assertThat(render(view).getPixel(c.x, c.y)).isNotEqualTo(Color.TRANSPARENT)
    }

    @Test
    fun configProperties_areUsableFromKotlin() {
        val config = ShowcaseConfig().apply {
            delay = 10
            maskColor = Color.RED
            titleTextColor = Color.GREEN
            shapePadding = 4
        }
        config.setRenderOverNavigationBar(true)
        assertThat(config.renderOverNavigationBar).isTrue()
        assertThat(config.maskColor).isEqualTo(Color.RED)
    }
}
