package uk.co.deanwild.materialshowcaseview

import android.graphics.Point
import android.graphics.Rect
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.common.truth.Truth.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import uk.co.deanwild.materialshowcaseview.shape.CircleShape
import uk.co.deanwild.materialshowcaseview.shape.NoShape
import uk.co.deanwild.materialshowcaseview.shape.OvalShape
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape
import uk.co.deanwild.materialshowcaseview.target.Target

@RunWith(AndroidJUnit4::class)
class ShapeTest {

    private fun target(bounds: Rect) = object : Target {
        override fun getPoint() = Point(bounds.centerX(), bounds.centerY())
        override fun getBounds() = bounds
    }

    @Test
    fun circle_radiusIsHalfTheLargestSide() {
        val shape = CircleShape(Rect(0, 0, 100, 40))
        assertThat(shape.radius).isEqualTo(50)
        assertThat(shape.getWidth()).isEqualTo(100)
        assertThat(shape.getHeight()).isEqualTo(100)
    }

    @Test
    fun circle_paddingAddsToTotalRadius() {
        val shape = CircleShape(20)
        shape.setPadding(5)
        assertThat(shape.getTotalRadius()).isEqualTo(25)
    }

    @Test
    fun circle_updateTarget_respectsAdjustToTarget() {
        val shape = CircleShape(20)
        shape.updateTarget(target(Rect(0, 0, 60, 60)))
        assertThat(shape.radius).isEqualTo(30)

        shape.isAdjustToTarget = false
        shape.updateTarget(target(Rect(0, 0, 200, 200)))
        assertThat(shape.radius).isEqualTo(30)
    }

    @Test
    fun oval_isHalfAsTallAsWide() {
        val shape = OvalShape(Rect(0, 0, 100, 20))
        assertThat(shape.getWidth()).isEqualTo(100)
        assertThat(shape.getHeight()).isEqualTo(50)
    }

    @Test
    fun oval_paddingAddsToTotalRadius() {
        val shape = OvalShape(40)
        shape.setPadding(10)
        assertThat(shape.getTotalRadius()).isEqualTo(50)
    }

    @Test
    fun rectangle_matchesTargetBounds() {
        val shape = RectangleShape(Rect(10, 10, 110, 60))
        assertThat(shape.getWidth()).isEqualTo(100)
        assertThat(shape.getHeight()).isEqualTo(50)
        shape.setPadding(4)
        assertThat(shape.getTotalRadius()).isEqualTo(29)
    }

    @Test
    fun rectangle_updateTarget_followsTarget() {
        val shape = RectangleShape(10, 10)
        shape.updateTarget(target(Rect(0, 0, 80, 30)))
        assertThat(shape.getWidth()).isEqualTo(80)
        assertThat(shape.getHeight()).isEqualTo(30)
    }

    @Test
    fun rectangle_fullWidth_coversAnyScreen() {
        val shape = RectangleShape(Rect(0, 0, 80, 30), true)
        assertThat(shape.getWidth()).isAtLeast(100_000)
        assertThat(shape.getHeight()).isEqualTo(30)
    }

    @Test
    fun noShape_isEmpty() {
        val shape = NoShape()
        shape.setPadding(10)
        assertThat(shape.getWidth()).isEqualTo(0)
        assertThat(shape.getHeight()).isEqualTo(0)
        assertThat(shape.getTotalRadius()).isEqualTo(0)
    }
}
