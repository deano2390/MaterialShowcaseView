package uk.co.deanwild.materialshowcaseview

import android.graphics.Color
import android.graphics.Typeface
import uk.co.deanwild.materialshowcaseview.shape.Shape

/**
 * Shared settings, typically applied to every item in a [MaterialShowcaseSequence].
 *
 * Only values that are explicitly set are applied, so a config never overrides
 * something you set on an individual showcase unless you set it here too.
 */
open class ShowcaseConfig {

    /** Delay before showing, in ms. -1 means "not set". */
    open var delay: Long = -1

    /** Fade/reveal duration, in ms. -1 means "not set". */
    open var fadeDuration: Long = -1

    open var maskColor: Int = Color.parseColor(DEFAULT_MASK_COLOUR)
        set(value) {
            field = value
            isMaskColorSet = true
        }

    open var titleTextColor: Int = Color.WHITE
        set(value) {
            field = value
            isTitleTextColorSet = true
        }

    open var contentTextColor: Int = Color.WHITE
        set(value) {
            field = value
            isContentTextColorSet = true
        }

    open var dismissTextColor: Int = Color.WHITE
        set(value) {
            field = value
            isDismissTextColorSet = true
        }

    open var dismissTextStyle: Typeface? = null

    open var shape: Shape? = null

    /** Padding between the target and the shape's edge. -1 means "not set". */
    open var shapePadding: Int = -1

    /** Whether to dim the navigation bar area too; null means "not set". */
    open var renderOverNavigationBar: Boolean? = null
        protected set

    /** Typeface for titles; null means "not set". */
    open var titleTextStyle: Typeface? = null

    /** Typeface for content text; null means "not set". */
    open var contentTextStyle: Typeface? = null

    /** Whether the back button closes showcases (ending a sequence); null means "not set". */
    open var dismissOnBackPress: Boolean? = null

    internal class TextSize(val unit: Int, val size: Float)

    internal var titleTextSize: TextSize? = null
        private set
    internal var contentTextSize: TextSize? = null
        private set
    internal var dismissTextSize: TextSize? = null
        private set

    /** Title text size, e.g. `setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)`. */
    open fun setTitleTextSize(unit: Int, size: Float) {
        titleTextSize = TextSize(unit, size)
    }

    open fun setContentTextSize(unit: Int, size: Float) {
        contentTextSize = TextSize(unit, size)
    }

    open fun setDismissTextSize(unit: Int, size: Float) {
        dismissTextSize = TextSize(unit, size)
    }

    // Keeps the exact 1.x signature (primitive boolean) for binary compatibility.
    open fun setRenderOverNavigationBar(renderOverNav: Boolean) {
        renderOverNavigationBar = renderOverNav
    }

    internal var isMaskColorSet = false
        private set
    internal var isTitleTextColorSet = false
        private set
    internal var isContentTextColorSet = false
        private set
    internal var isDismissTextColorSet = false
        private set

    // Kotlin source compatibility with 1.x, where these were Java getters/setters
    // callable as methods from Kotlin. Hidden from Java, which uses the property accessors.

    @JvmSynthetic @JvmName("getDelay\$compat")
    @Deprecated("Use the delay property", ReplaceWith("delay"))
    fun getDelay(): Long = delay

    @JvmSynthetic @JvmName("setDelay\$compat")
    @Deprecated("Use the delay property", ReplaceWith("delay = value"))
    fun setDelay(value: Long) {
        delay = value
    }

    @JvmSynthetic @JvmName("getFadeDuration\$compat")
    @Deprecated("Use the fadeDuration property", ReplaceWith("fadeDuration"))
    fun getFadeDuration(): Long = fadeDuration

    @JvmSynthetic @JvmName("setFadeDuration\$compat")
    @Deprecated("Use the fadeDuration property", ReplaceWith("fadeDuration = value"))
    fun setFadeDuration(value: Long) {
        fadeDuration = value
    }

    @JvmSynthetic @JvmName("getMaskColor\$compat")
    @Deprecated("Use the maskColor property", ReplaceWith("maskColor"))
    fun getMaskColor(): Int = maskColor

    @JvmSynthetic @JvmName("setMaskColor\$compat")
    @Deprecated("Use the maskColor property", ReplaceWith("maskColor = value"))
    fun setMaskColor(value: Int) {
        maskColor = value
    }

    @JvmSynthetic @JvmName("getTitleTextColor\$compat")
    @Deprecated("Use the titleTextColor property", ReplaceWith("titleTextColor"))
    fun getTitleTextColor(): Int = titleTextColor

    @JvmSynthetic @JvmName("setTitleTextColor\$compat")
    @Deprecated("Use the titleTextColor property", ReplaceWith("titleTextColor = value"))
    fun setTitleTextColor(value: Int) {
        titleTextColor = value
    }

    @JvmSynthetic @JvmName("getContentTextColor\$compat")
    @Deprecated("Use the contentTextColor property", ReplaceWith("contentTextColor"))
    fun getContentTextColor(): Int = contentTextColor

    @JvmSynthetic @JvmName("setContentTextColor\$compat")
    @Deprecated("Use the contentTextColor property", ReplaceWith("contentTextColor = value"))
    fun setContentTextColor(value: Int) {
        contentTextColor = value
    }

    @JvmSynthetic @JvmName("getDismissTextColor\$compat")
    @Deprecated("Use the dismissTextColor property", ReplaceWith("dismissTextColor"))
    fun getDismissTextColor(): Int = dismissTextColor

    @JvmSynthetic @JvmName("setDismissTextColor\$compat")
    @Deprecated("Use the dismissTextColor property", ReplaceWith("dismissTextColor = value"))
    fun setDismissTextColor(value: Int) {
        dismissTextColor = value
    }

    @JvmSynthetic @JvmName("getDismissTextStyle\$compat")
    @Deprecated("Use the dismissTextStyle property", ReplaceWith("dismissTextStyle"))
    fun getDismissTextStyle(): Typeface? = dismissTextStyle

    @JvmSynthetic @JvmName("setDismissTextStyle\$compat")
    @Deprecated("Use the dismissTextStyle property", ReplaceWith("dismissTextStyle = value"))
    fun setDismissTextStyle(value: Typeface?) {
        dismissTextStyle = value
    }

    @JvmSynthetic @JvmName("getShape\$compat")
    @Deprecated("Use the shape property", ReplaceWith("shape"))
    fun getShape(): Shape? = shape

    @JvmSynthetic @JvmName("setShape\$compat")
    @Deprecated("Use the shape property", ReplaceWith("shape = value"))
    fun setShape(value: Shape?) {
        shape = value
    }

    @JvmSynthetic @JvmName("getShapePadding\$compat")
    @Deprecated("Use the shapePadding property", ReplaceWith("shapePadding"))
    fun getShapePadding(): Int = shapePadding

    @JvmSynthetic @JvmName("setShapePadding\$compat")
    @Deprecated("Use the shapePadding property", ReplaceWith("shapePadding = value"))
    fun setShapePadding(value: Int) {
        shapePadding = value
    }

    @JvmSynthetic @JvmName("getRenderOverNavigationBar\$compat")
    @Deprecated("Use the renderOverNavigationBar property", ReplaceWith("renderOverNavigationBar"))
    fun getRenderOverNavigationBar(): Boolean? = renderOverNavigationBar


    companion object {
        const val DEFAULT_MASK_COLOUR = "#dd335075"
    }
}
