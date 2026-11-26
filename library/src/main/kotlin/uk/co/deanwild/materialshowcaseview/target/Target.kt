package uk.co.deanwild.materialshowcaseview.target

import android.graphics.Point
import android.graphics.Rect

/** Something to showcase. Coordinates are in the window's coordinate space. */
interface Target {

    /** Centre of the target, in window coordinates. */
    fun getPoint(): Point

    /** Bounds of the target, in window coordinates. */
    fun getBounds(): Rect

    companion object {
        /** No target: the showcase masks the whole screen and centres its content. */
        @JvmField
        val NONE: Target = object : Target {
            override fun getPoint() = Point(1_000_000, 1_000_000)

            override fun getBounds(): Rect {
                val p = getPoint()
                return Rect(p.x - 190, p.y - 190, p.x + 190, p.y + 190)
            }
        }
    }
}
