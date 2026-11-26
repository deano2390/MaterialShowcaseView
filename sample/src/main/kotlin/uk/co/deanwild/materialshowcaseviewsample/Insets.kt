@file:JvmName("Insets")

package uk.co.deanwild.materialshowcaseviewsample

import android.app.Activity
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Apps targeting SDK 35+ are drawn edge-to-edge, behind the system bars (and the action bar).
 * Pad the content so it stays clear of them; the showcase overlay handles insets on its own.
 */
fun Activity.padContentForSystemBars() {
    val content = findViewById<ViewGroup>(android.R.id.content).getChildAt(0) ?: return
    val initial = intArrayOf(content.paddingLeft, content.paddingTop, content.paddingRight, content.paddingBottom)
    val actionBarHeight = if ((this as? AppCompatActivity)?.supportActionBar?.isShowing == true) {
        TypedValue().let { tv ->
            theme.resolveAttribute(androidx.appcompat.R.attr.actionBarSize, tv, true)
            TypedValue.complexToDimensionPixelSize(tv.data, resources.displayMetrics)
        }
    } else {
        0
    }
    ViewCompat.setOnApplyWindowInsetsListener(content) { v: View, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        v.updatePadding(
            left = initial[0] + bars.left,
            top = initial[1] + bars.top + actionBarHeight,
            right = initial[2] + bars.right,
            bottom = initial[3] + bars.bottom,
        )
        insets
    }
}
