package uk.co.deanwild.materialshowcaseview.target

import android.app.Activity
import android.graphics.Point
import android.graphics.Rect
import android.view.View

/** Targets a [View]. */
open class ViewTarget(open val view: View) : Target {

    constructor(viewId: Int, activity: Activity) : this(
        requireNotNull(activity.findViewById<View>(viewId)) { "No view with id $viewId in ${activity.javaClass.name}" },
    )

    override fun getPoint(): Point {
        val bounds = getBounds()
        return Point(bounds.centerX(), bounds.centerY())
    }

    override fun getBounds(): Rect {
        val location = IntArray(2)
        view.getLocationInWindow(location)
        // Use the laid-out size (not the measured size) so bounds always match what is on screen.
        return Rect(location[0], location[1], location[0] + view.width, location[1] + view.height)
    }

    // Kotlin source compatibility with 1.x, where these were Java getters/setters
    // callable as methods from Kotlin. Hidden from Java, which uses the property accessors.

    @JvmSynthetic @JvmName("getView\$compat")
    @Deprecated("Use the view property", ReplaceWith("view"))
    fun getView(): View = view
}
