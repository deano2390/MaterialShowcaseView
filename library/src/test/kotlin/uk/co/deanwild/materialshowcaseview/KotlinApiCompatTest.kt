package uk.co.deanwild.materialshowcaseview

import android.app.Activity
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Point
import android.graphics.Rect
import android.view.View
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.deanwild.materialshowcaseview.shape.Shape
import uk.co.deanwild.materialshowcaseview.target.Target

/**
 * Source-compatibility guard for Kotlin callers written against 1.x, where everything was Java
 * with platform types: nullable arguments and both nullable/non-null override styles compile.
 */
@RunWith(AndroidJUnit4::class)
class KotlinApiCompatTest {

    // Overrides with nullable parameters (what Android Studio generated against 1.x)...
    private class NullableListener : IShowcaseListener {
        override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) = Unit
        override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) = Unit
    }

    // ...and with non-null parameters.
    private class NonNullListener : IShowcaseListener {
        override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView) = Unit
        override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView) = Unit
    }

    private class NullableShape : Shape {
        override fun draw(canvas: Canvas?, paint: Paint?, x: Int, y: Int) = Unit
        override fun getWidth() = 0
        override fun getHeight() = 0
        override fun updateTarget(target: Target?) = Unit
        override fun getTotalRadius() = 0
        override fun setPadding(padding: Int) = Unit
    }

    private class NullableTarget : Target {
        override fun getPoint(): Point? = Point()
        override fun getBounds(): Rect? = Rect()
    }

    private class NullableFactory : IAnimationFactory {
        override fun animateInView(target: View?, point: Point?, duration: Long, listener: IAnimationFactory.AnimationStartListener?) = Unit
        override fun animateOutView(target: View?, point: Point?, duration: Long, listener: IAnimationFactory.AnimationEndListener?) = Unit
        override fun animateTargetToPoint(showcaseView: MaterialShowcaseView?, point: Point?) = Unit
    }

    @Test
    fun nullableArgumentsCompile_likeInAFragment() {
        val screen = TestScreen()
        val activity: Activity? = screen.activity // e.g. Fragment.activity
        val view: View? = screen.middle

        val showcase = MaterialShowcaseView.Builder(activity)
            .setTarget(view)
            .setListener(NullableListener())
            .setListener(NonNullListener())
            .setShape(NullableShape())
            .build()
        showcase.setAnimationFactory(NullableFactory())
        showcase.setTarget(NullableTarget())
        showcase.show(activity)

        val sequence = MaterialShowcaseSequence(activity, "kotlin-compat")
        sequence.addSequenceItem(view, "content", "GOT IT")
        sequence.setOnItemShownListener { itemView, position -> itemView.context; position }
        sequence.setOnItemDismissedListener(object : MaterialShowcaseSequence.OnSequenceItemDismissedListener {
            override fun onDismiss(itemView: MaterialShowcaseView?, position: Int) = Unit
        })
        sequence.setOnItemShownListener(object : MaterialShowcaseSequence.OnSequenceItemShownListener {
            override fun onShow(itemView: MaterialShowcaseView, position: Int) = Unit
        })
        ShowcaseTooltip.build(activity as android.content.Context?)
        MaterialShowcaseView.resetAll(activity)
    }

    @Test
    fun shapeAndTargetGetters_areProperties() {
        val shape: Shape = uk.co.deanwild.materialshowcaseview.shape.CircleShape(10)
        assertThat(shape.width).isEqualTo(20)
        assertThat(Target.NONE.point.x).isEqualTo(1_000_000)
    }
}
