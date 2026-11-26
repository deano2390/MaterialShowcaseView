package uk.co.deanwild.materialshowcaseview

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.GraphicsMode

/** Renders the overlay with real Skia and checks the mask and the "hole" over the target. */
@RunWith(AndroidJUnit4::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class RenderingTest {

    private val screen = TestScreen()
    private val mask = 0xDD335075.toInt()

    private fun render(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        view.draw(Canvas(bitmap))
        return bitmap
    }

    private fun show(target: View): MaterialShowcaseView {
        val view = MaterialShowcaseView.Builder(screen.activity)
            .setTarget(target)
            .setMaskColour(mask)
            .build()
        view.setAnimationFactory(CountingAnimationFactory())
        view.show(screen.activity)
        idle()
        return view
    }

    /** Position of [target]'s centre in [overlay]'s own coordinate space. */
    private fun centreIn(overlay: View, target: View): Pair<Int, Int> {
        val o = IntArray(2).also { overlay.getLocationInWindow(it) }
        val c = target.windowCentre()
        return (c.x - o[0]) to (c.y - o[1])
    }

    @Test
    fun maskIsDrawn_andTargetIsCutOut() {
        val view = show(screen.middle)
        val bitmap = render(view)

        val (x, y) = centreIn(view, screen.middle)
        assertThat(Color.alpha(bitmap.getPixel(x, y))).isEqualTo(0)
        assertThat(bitmap.getPixel(2, 2)).isEqualTo(mask)
    }

    @Test
    fun hole_isCorrect_whenOverlayIsOffsetInWindow() {
        // Some windows (dialog-themed activities, decor padding, cut-outs) place the overlay
        // away from the window origin; the hole must still line up with the target. (PR #170)
        screen.decor.setPadding(0, 120, 0, 0)
        screen.decor.requestLayout()
        idle()

        val view = show(screen.middle)
        val bitmap = render(view)

        val (x, y) = centreIn(view, screen.middle)
        assertThat(Color.alpha(bitmap.getPixel(x, y))).isEqualTo(0)
    }

    @Test
    fun drawingTwice_atSameSize_isStable() {
        val view = show(screen.middle)
        val a = render(view)
        val b = render(view)
        assertThat(a.sameAs(b)).isTrue()
    }
}
