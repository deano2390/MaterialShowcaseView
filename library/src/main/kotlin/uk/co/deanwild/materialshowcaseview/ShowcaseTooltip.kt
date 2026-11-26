@file:Suppress("DEPRECATION") // android.app.Fragment is kept in MyContext for source compatibility.

package uk.co.deanwild.materialshowcaseview

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.app.DialogFragment
import android.app.Fragment
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.text.HtmlCompat

/**
 * A speech-bubble tooltip pointing at the showcase target.
 *
 * Based on original code by florentchampigny: https://github.com/florent37/ViewTooltip
 */
open class ShowcaseTooltip private constructor(context: Context) {

    private var rootView: ViewGroup? = null
    private var view: View? = null
    private val activity: Activity? = getActivityContext(context)
    private val tooltipView: TooltipView = TooltipView(MyContext(activity ?: context).getContext())

    /** Anchors the tooltip to [view], drawing it inside [rootView]. Called by [MaterialShowcaseView]. */
    open fun configureTarget(rootView: ViewGroup, view: View) {
        this.rootView = rootView
        this.view = view
    }

    open fun position(position: Position): ShowcaseTooltip = apply { tooltipView.setPosition(position) }

    open fun customView(customView: View?): ShowcaseTooltip =
        apply { tooltipView.setCustomView(requireNotNull(customView)) }

    /** Uses the view with [viewId] from the Activity as the tooltip content. */
    open fun customView(viewId: Int): ShowcaseTooltip = apply {
        val custom = (activity ?: view?.context as? Activity)?.findViewById<View>(viewId)
            ?: throw IllegalArgumentException("No view with id $viewId found")
        tooltipView.setCustomView(custom)
    }

    open fun arrowWidth(arrowWidth: Int): ShowcaseTooltip = apply { tooltipView.arrowWidth = arrowWidth }

    open fun arrowHeight(arrowHeight: Int): ShowcaseTooltip = apply { tooltipView.arrowHeight = arrowHeight }

    open fun arrowSourceMargin(arrowSourceMargin: Int): ShowcaseTooltip =
        apply { tooltipView.arrowSourceMargin = arrowSourceMargin }

    open fun arrowTargetMargin(arrowTargetMargin: Int): ShowcaseTooltip =
        apply { tooltipView.arrowTargetMargin = arrowTargetMargin }

    open fun align(align: ALIGN): ShowcaseTooltip = apply { tooltipView.setAlign(align) }

    /**
     * Shows the tooltip next to the configured target, [margin] pixels away from it.
     * [configureTarget] must have been called first.
     */
    open fun show(margin: Int): TooltipView {
        val anchor = view ?: return tooltipView
        val root = rootView ?: (activity?.window?.decorView as? ViewGroup) ?: return tooltipView

        anchor.postDelayed({
            if (!root.isAttachedToWindow || !anchor.isAttachedToWindow) return@postDelayed

            // Target bounds relative to where the root lays out its children, i.e. inside its
            // padding (the showcase pads itself by the system bar insets).
            val anchorLocation = IntArray(2).also { anchor.getLocationInWindow(it) }
            val rootLocation = IntArray(2).also { root.getLocationInWindow(it) }
            val left = anchorLocation[0] - rootLocation[0] - root.paddingLeft
            val top = anchorLocation[1] - rootLocation[1] - root.paddingTop
            val rect = Rect(left, top - margin, left + anchor.width, top + anchor.height + margin)
            val availableWidth = root.width - root.paddingLeft - root.paddingRight

            (tooltipView.parent as? ViewGroup)?.removeView(tooltipView)
            root.addView(tooltipView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            tooltipView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                override fun onPreDraw(): Boolean {
                    tooltipView.setup(rect, availableWidth)
                    tooltipView.viewTreeObserver.removeOnPreDrawListener(this)
                    return false
                }
            })
        }, SHOW_DELAY_MS)
        return tooltipView
    }

    open fun color(color: Int): ShowcaseTooltip = apply { tooltipView.setColor(color) }

    open fun color(paint: Paint): ShowcaseTooltip = apply { tooltipView.setPaint(paint) }

    open fun onDisplay(listener: ListenerDisplay?): ShowcaseTooltip = apply { tooltipView.setListenerDisplay(listener) }

    open fun padding(left: Int, top: Int, right: Int, bottom: Int): ShowcaseTooltip = apply {
        tooltipView.bubblePaddingTop = top
        tooltipView.bubblePaddingBottom = bottom
        tooltipView.bubblePaddingLeft = left
        tooltipView.bubblePaddingRight = right
    }

    open fun animation(tooltipAnimation: TooltipAnimation): ShowcaseTooltip =
        apply { tooltipView.setTooltipAnimation(tooltipAnimation) }

    /** Sets the tooltip text. Simple HTML markup is supported. */
    open fun text(text: String?): ShowcaseTooltip = apply { tooltipView.setText(text) }

    open fun text(text: Int): ShowcaseTooltip = apply { tooltipView.setText(text) }

    open fun corner(corner: Int): ShowcaseTooltip = apply { tooltipView.setCorner(corner) }

    open fun textColor(textColor: Int): ShowcaseTooltip = apply { tooltipView.setTextColor(textColor) }

    open fun textTypeFace(typeface: Typeface?): ShowcaseTooltip = apply { tooltipView.setTextTypeFace(typeface) }

    open fun textSize(unit: Int, textSize: Float): ShowcaseTooltip = apply { tooltipView.setTextSize(unit, textSize) }

    open fun setTextGravity(textGravity: Int): ShowcaseTooltip = apply { tooltipView.setTextGravity(textGravity) }

    open fun distanceWithView(distance: Int): ShowcaseTooltip = apply { tooltipView.setDistanceWithView(distance) }

    open fun border(color: Int, width: Float): ShowcaseTooltip = apply {
        tooltipView.setBorderPaint(
            Paint(Paint.ANTI_ALIAS_FLAG).apply {
                this.color = color
                style = Paint.Style.STROKE
                strokeWidth = width
            },
        )
    }

    enum class Position { LEFT, RIGHT, TOP, BOTTOM }

    @Suppress("ClassName")
    enum class ALIGN { START, CENTER, END }

    interface TooltipAnimation {
        fun animateEnter(view: View, animatorListener: Animator.AnimatorListener)
        fun animateExit(view: View, animatorListener: Animator.AnimatorListener)
    }

    fun interface ListenerDisplay {
        fun onDisplay(view: View)
    }

    open class FadeTooltipAnimation @JvmOverloads constructor(
        private val fadeDuration: Long = 400,
    ) : TooltipAnimation {

        override fun animateEnter(view: View, animatorListener: Animator.AnimatorListener) {
            view.alpha = 0f
            view.animate().alpha(1f).setDuration(fadeDuration).setListener(animatorListener)
        }

        override fun animateExit(view: View, animatorListener: Animator.AnimatorListener) {
            view.animate().alpha(0f).setDuration(fadeDuration).setListener(animatorListener)
        }
    }

    open class TooltipView(context: Context) : FrameLayout(context) {

        open var arrowHeight = 15
            set(value) {
                field = value
                postInvalidate()
            }
        open var arrowWidth = 15
            set(value) {
                field = value
                postInvalidate()
            }
        open var arrowSourceMargin = 0
            set(value) {
                field = value
                postInvalidate()
            }
        open var arrowTargetMargin = 0
            set(value) {
                field = value
                postInvalidate()
            }

        protected var childView: View = TextView(context).apply { setTextColor(Color.BLACK) }
        private var bubblePath: Path? = null
        private var bubblePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }
        private var borderPaint: Paint? = null
        private var position = Position.BOTTOM
        private var align = ALIGN.CENTER
        private var listenerDisplay: ListenerDisplay? = null
        private var tooltipAnimation: TooltipAnimation = FadeTooltipAnimation()
        private var corner = 30

        internal var bubblePaddingTop = 20
        internal var bubblePaddingBottom = 30
        internal var bubblePaddingRight = 60
        internal var bubblePaddingLeft = 60

        private var viewRect: Rect? = null
        private var distanceWithView = 0

        init {
            setWillNotDraw(false)
            addView(childView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
            childView.setPadding(0, 0, 0, 0)
            setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint)
        }

        open fun setCustomView(customView: View) {
            removeView(childView)
            // A view looked up by id is already in the layout; move it into the tooltip.
            (customView.parent as? ViewGroup)?.removeView(customView)
            childView = customView
            addView(childView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }

        open fun setColor(color: Int) {
            bubblePaint.color = color
            postInvalidate()
        }

        open fun setPaint(paint: Paint) {
            bubblePaint = paint
            setLayerType(LAYER_TYPE_SOFTWARE, paint)
            postInvalidate()
        }

        open fun setPosition(position: Position) {
            this.position = position
            when (position) {
                Position.TOP -> setPadding(
                    bubblePaddingLeft, bubblePaddingTop, bubblePaddingRight, bubblePaddingBottom + arrowHeight,
                )
                Position.BOTTOM -> setPadding(
                    bubblePaddingLeft, bubblePaddingTop + arrowHeight, bubblePaddingRight, bubblePaddingBottom,
                )
                Position.LEFT -> setPadding(
                    bubblePaddingLeft, bubblePaddingTop, bubblePaddingRight + arrowHeight, bubblePaddingBottom,
                )
                Position.RIGHT -> setPadding(
                    bubblePaddingLeft + arrowHeight, bubblePaddingTop, bubblePaddingRight, bubblePaddingBottom,
                )
            }
            postInvalidate()
        }

        open fun setAlign(align: ALIGN) {
            this.align = align
            postInvalidate()
        }

        open fun setText(text: String?) {
            (childView as? TextView)?.text = HtmlCompat.fromHtml(text.orEmpty(), HtmlCompat.FROM_HTML_MODE_LEGACY)
            postInvalidate()
        }

        open fun setText(text: Int) {
            (childView as? TextView)?.setText(text)
            postInvalidate()
        }

        open fun setTextColor(textColor: Int) {
            (childView as? TextView)?.setTextColor(textColor)
            postInvalidate()
        }

        open fun setTextTypeFace(textTypeFace: Typeface?) {
            (childView as? TextView)?.typeface = textTypeFace
            postInvalidate()
        }

        open fun setTextSize(unit: Int, size: Float) {
            (childView as? TextView)?.setTextSize(unit, size)
            postInvalidate()
        }

        open fun setTextGravity(textGravity: Int) {
            (childView as? TextView)?.gravity = textGravity
            postInvalidate()
        }

        open fun setCorner(corner: Int) {
            this.corner = corner
        }

        override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
            super.onSizeChanged(w, h, oldw, oldh)
            bubblePath = drawBubble(RectF(0f, 0f, w.toFloat(), h.toFloat()), corner.toFloat())
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            bubblePath?.let { path ->
                canvas.drawPath(path, bubblePaint)
                borderPaint?.let { canvas.drawPath(path, it) }
            }
        }

        open fun setListenerDisplay(listener: ListenerDisplay?) {
            listenerDisplay = listener
        }

        open fun setTooltipAnimation(tooltipAnimation: TooltipAnimation) {
            this.tooltipAnimation = tooltipAnimation
        }

        protected open fun startEnterAnimation() {
            tooltipAnimation.animateEnter(this, object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    listenerDisplay?.onDisplay(this@TooltipView)
                }
            })
        }

        open fun setupPosition(rect: Rect) {
            val x: Int
            val y: Int
            if (position == Position.LEFT || position == Position.RIGHT) {
                x = if (position == Position.LEFT) rect.left - width - distanceWithView else rect.right + distanceWithView
                y = rect.top + getAlignOffset(height, rect.height())
            } else {
                y = if (position == Position.BOTTOM) rect.bottom + distanceWithView else rect.top - height - distanceWithView
                x = rect.left + getAlignOffset(width, rect.width())
            }
            translationX = x.toFloat()
            translationY = y.toFloat()
        }

        private fun getAlignOffset(myLength: Int, hisLength: Int): Int = when (align) {
            ALIGN.END -> hisLength - myLength
            ALIGN.CENTER -> (hisLength - myLength) / 2
            ALIGN.START -> 0
        }

        private fun drawBubble(myRect: RectF, cornerDiameter: Float): Path {
            val path = Path()
            val viewRect = viewRect ?: return path
            val diameter = cornerDiameter.coerceAtLeast(0f)

            val spacingTop = if (position == Position.BOTTOM) arrowHeight.toFloat() else 0f
            val spacingBottom = if (position == Position.TOP) arrowHeight.toFloat() else 0f

            val left = BUBBLE_SPACING + myRect.left
            val top = spacingTop + myRect.top
            val right = myRect.right - BUBBLE_SPACING
            val bottom = myRect.bottom - spacingBottom
            val centerX = viewRect.centerX() - x

            val vertical = position == Position.TOP || position == Position.BOTTOM
            val arrowSourceX = if (vertical) centerX + arrowSourceMargin else centerX
            val arrowTargetX = if (vertical) centerX + arrowTargetMargin else centerX
            val arrowSourceY = if (!vertical) bottom / 2f - arrowSourceMargin else bottom / 2f
            val arrowTargetY = if (!vertical) bottom / 2f - arrowTargetMargin else bottom / 2f
            val half = diameter / 2f

            path.moveTo(left + half, top)
            if (position == Position.BOTTOM) {
                path.lineTo(arrowSourceX - arrowWidth, top)
                path.lineTo(arrowTargetX, myRect.top)
                path.lineTo(arrowSourceX + arrowWidth, top)
            }
            path.lineTo(right - half, top)
            path.quadTo(right, top, right, top + half)

            if (position == Position.LEFT) {
                path.lineTo(right, arrowSourceY - arrowWidth)
                path.lineTo(myRect.right, arrowTargetY)
                path.lineTo(right, arrowSourceY + arrowWidth)
            }
            path.lineTo(right, bottom - half)
            path.quadTo(right, bottom, right - half, bottom)

            if (position == Position.TOP) {
                path.lineTo(arrowSourceX + arrowWidth, bottom)
                path.lineTo(arrowTargetX, myRect.bottom)
                path.lineTo(arrowSourceX - arrowWidth, bottom)
            }
            path.lineTo(left + half, bottom)
            path.quadTo(left, bottom, left, bottom - half)

            if (position == Position.RIGHT) {
                path.lineTo(left, arrowSourceY + arrowWidth)
                path.lineTo(myRect.left, arrowTargetY)
                path.lineTo(left, arrowSourceY - arrowWidth)
            }
            path.lineTo(left, top + half)
            path.quadTo(left, top, left + half, top)
            path.close()
            return path
        }

        /** Shrinks or shifts the tooltip so it fits on screen. Returns true if anything changed. */
        open fun adjustSize(rect: Rect, screenWidth: Int): Boolean {
            var changed = false
            val layoutParams = layoutParams ?: return false
            if (position == Position.LEFT && width > rect.left) {
                layoutParams.width = rect.left - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView
                changed = true
            } else if (position == Position.RIGHT && rect.right + width > screenWidth) {
                layoutParams.width = screenWidth - rect.right - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView
                changed = true
            } else if (position == Position.TOP || position == Position.BOTTOM) {
                var adjustedLeft = rect.left
                var adjustedRight = rect.right
                if (rect.centerX() + width / 2f > screenWidth) {
                    val diff = (rect.centerX() + width / 2f - screenWidth).toInt()
                    adjustedLeft -= diff
                    adjustedRight -= diff
                    setAlign(ALIGN.CENTER)
                    changed = true
                } else if (rect.centerX() - width / 2f < 0) {
                    val diff = (-(rect.centerX() - width / 2f)).toInt()
                    adjustedLeft += diff
                    adjustedRight += diff
                    setAlign(ALIGN.CENTER)
                    changed = true
                }
                rect.left = adjustedLeft.coerceAtLeast(0)
                rect.right = adjustedRight.coerceAtMost(screenWidth)
            }
            this.layoutParams = layoutParams
            postInvalidate()
            return changed
        }

        private fun onSetup(myRect: Rect) {
            setupPosition(myRect)
            bubblePath = drawBubble(RectF(0f, 0f, width.toFloat(), height.toFloat()), corner.toFloat())
            startEnterAnimation()
        }

        open fun setup(viewRect: Rect, screenWidth: Int) {
            this.viewRect = Rect(viewRect)
            val myRect = Rect(viewRect)
            if (!adjustSize(myRect, screenWidth)) {
                onSetup(myRect)
            } else {
                viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
                    override fun onPreDraw(): Boolean {
                        onSetup(myRect)
                        viewTreeObserver.removeOnPreDrawListener(this)
                        return false
                    }
                })
            }
        }

        open fun removeNow() {
            (parent as? ViewGroup)?.removeView(this)
        }

        open fun closeNow() = removeNow()

        open fun setDistanceWithView(distanceWithView: Int) {
            this.distanceWithView = distanceWithView
        }

        open fun setBorderPaint(borderPaint: Paint?) {
            this.borderPaint = borderPaint
            postInvalidate()
        }

        // Kotlin source compatibility with 1.x, where these were Java getters/setters
        // callable as methods from Kotlin. Hidden from Java, which uses the property accessors.

        @JvmSynthetic @JvmName("getArrowHeight\$compat")
        @Deprecated("Use the arrowHeight property", ReplaceWith("arrowHeight"))
        fun getArrowHeight(): Int = arrowHeight

        @JvmSynthetic @JvmName("setArrowHeight\$compat")
        @Deprecated("Use the arrowHeight property", ReplaceWith("arrowHeight = value"))
        fun setArrowHeight(value: Int) {
            arrowHeight = value
        }

        @JvmSynthetic @JvmName("getArrowWidth\$compat")
        @Deprecated("Use the arrowWidth property", ReplaceWith("arrowWidth"))
        fun getArrowWidth(): Int = arrowWidth

        @JvmSynthetic @JvmName("setArrowWidth\$compat")
        @Deprecated("Use the arrowWidth property", ReplaceWith("arrowWidth = value"))
        fun setArrowWidth(value: Int) {
            arrowWidth = value
        }

        @JvmSynthetic @JvmName("getArrowSourceMargin\$compat")
        @Deprecated("Use the arrowSourceMargin property", ReplaceWith("arrowSourceMargin"))
        fun getArrowSourceMargin(): Int = arrowSourceMargin

        @JvmSynthetic @JvmName("setArrowSourceMargin\$compat")
        @Deprecated("Use the arrowSourceMargin property", ReplaceWith("arrowSourceMargin = value"))
        fun setArrowSourceMargin(value: Int) {
            arrowSourceMargin = value
        }

        @JvmSynthetic @JvmName("getArrowTargetMargin\$compat")
        @Deprecated("Use the arrowTargetMargin property", ReplaceWith("arrowTargetMargin"))
        fun getArrowTargetMargin(): Int = arrowTargetMargin

        @JvmSynthetic @JvmName("setArrowTargetMargin\$compat")
        @Deprecated("Use the arrowTargetMargin property", ReplaceWith("arrowTargetMargin = value"))
        fun setArrowTargetMargin(value: Int) {
            arrowTargetMargin = value
        }

        private companion object {
            const val MARGIN_SCREEN_BORDER_TOOLTIP = 30
            const val BUBBLE_SPACING = 30f
        }
    }

    /** Resolves the Activity/Window the tooltip belongs to. */
    open class MyContext {
        private var fragment: Fragment? = null
        private var context: Context? = null
        private var activity: Activity? = null

        constructor(activity: Activity) {
            this.activity = activity
        }

        @Deprecated("android.app.Fragment is deprecated; pass an Activity or Context instead.")
        constructor(fragment: Fragment) {
            this.fragment = fragment
        }

        constructor(context: Context) {
            this.activity = context as? Activity
            this.context = context
        }

        open fun getContext(): Context =
            activity ?: fragment?.activity ?: context ?: error("MyContext has no Context")

        open fun getActivity(): Activity? = activity ?: fragment?.activity ?: getActivityContext(context)

        open fun getWindow(): Window? {
            activity?.let { return it.window }
            (fragment as? DialogFragment)?.dialog?.window?.let { return it }
            return getActivity()?.window
        }
    }

    companion object {
        private const val SHOW_DELAY_MS = 100L

        @JvmStatic
        fun build(context: Context?): ShowcaseTooltip =
            ShowcaseTooltip(requireNotNull(context) { "ShowcaseTooltip.build() needs a Context" })

        private fun getActivityContext(context: Context?): Activity? {
            var c = context
            while (c is ContextWrapper) {
                if (c is Activity) return c
                c = c.baseContext
            }
            return null
        }
    }
}
