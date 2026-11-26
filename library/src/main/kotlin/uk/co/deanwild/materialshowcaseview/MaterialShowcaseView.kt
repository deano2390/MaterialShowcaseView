package uk.co.deanwild.materialshowcaseview

import android.animation.ValueAnimator
import android.app.Activity
import android.app.Dialog
import android.content.ContextWrapper
import android.graphics.PixelFormat
import android.os.Build
import android.view.WindowManager
import android.view.animation.AccelerateDecelerateInterpolator
import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.RippleDrawable
import android.content.res.ColorStateList
import android.os.Looper
import android.util.AttributeSet
import android.view.Gravity
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.ComponentDialog
import androidx.activity.OnBackPressedCallback
import androidx.annotation.VisibleForTesting
import androidx.core.graphics.Insets
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.doOnLayout
import uk.co.deanwild.materialshowcaseview.shape.CircleShape
import uk.co.deanwild.materialshowcaseview.shape.NoShape
import uk.co.deanwild.materialshowcaseview.shape.OvalShape
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape
import uk.co.deanwild.materialshowcaseview.shape.RoundedRectangleShape
import uk.co.deanwild.materialshowcaseview.shape.Shape
import uk.co.deanwild.materialshowcaseview.target.Target
import uk.co.deanwild.materialshowcaseview.target.ViewTarget

/**
 * A full-screen overlay that dims the screen, cuts a hole around a target and explains it.
 *
 * Usually created with [Builder]; use [MaterialShowcaseSequence] to show several in turn.
 */
open class MaterialShowcaseView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), View.OnTouchListener, View.OnClickListener {

    private var target: Target? = null
    private var shape: Shape? = null

    /** Target centre in this view's own coordinates. */
    internal var showcaseX = 0
        private set
    internal var showcaseY = 0
        private set

    private var wasDismissed = false
    private var wasSkipped = false
    private var dismissing = false
    private var notifiedDismissed = false
    private var displayed = false

    private var shapePadding = DEFAULT_SHAPE_PADDING
    private var tooltipMargin = DEFAULT_TOOLTIP_MARGIN

    /** The block of text and buttons placed beside the target: built in, or the app's own. */
    private var contentBox: View
    private val builtInContent: View
    private var hasCustomContent = false
    private var scrollTargetIntoView = false
    private val titleTextView: TextView
    private val contentTextView: TextView
    private val dismissButton: TextView
    private val skipButton: TextView

    private var hasCustomGravity = false
    private var contentGravity = Gravity.TOP
    private var contentBottomMargin = 0
    private var contentTopMargin = 0

    private var dismissOnTouch = false
    private var shouldRender = false
    private var renderOverNav = false
    private var maskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR)
    private var animationFactory: IAnimationFactory = CircularRevealAnimationFactory()
    private var hasCustomAnimationFactory = false
    private var shouldAnimate = true
    private var useFadeAnimation = false
    private var fadeDurationInMillis = DEFAULT_FADE_TIME
    private var delayInMillis = DEFAULT_DELAY
    private var singleUse = false
    private var prefsManager: PrefsManager? = null
    private val listeners = mutableListOf<IShowcaseListener>()
    private var detachedListener: IDetachedListener? = null
    private var targetTouchable = false
    private var dismissOnTargetTouch = true
    private var isSequence = false

    private var toolTip: ShowcaseTooltip? = null
    private var toolTipShown = false

    /** System bar + display cut-out insets, and the navigation buttons' insets, relative to this view. */
    private var systemInsets = Insets.NONE
    private var navigationButtonInsets = Insets.NONE

    /** Siblings this showcase has hidden from accessibility services (see [AccessibilityHider]). */
    private val hiddenForAccessibility = mutableListOf<View>()

    /** Outline drawn around the shape; null for none. */
    private var borderPaint: Paint? = null

    private val eraser = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val layoutListener = ViewTreeObserver.OnGlobalLayoutListener { updateLayout() }

    // Keeps the highlight on the target when the content underneath scrolls.
    private val scrollListener = ViewTreeObserver.OnScrollChangedListener { updateLayout() }

    private var dismissOnBackPress = false
    private var backCallback: OnBackPressedCallback? = null

    /** The Activity this showcase is shown in (directly, or over one of its dialogs). */
    private var hostActivity: Activity? = null

    /** When shown over a dialog: the dialog window's root view, and the panel window we add. */
    private var hostRoot: View? = null
    private var panel: FrameLayout? = null
    private var panelWindowManager: WindowManager? = null
    private val hostDetachListener = object : OnAttachStateChangeListener {
        override fun onViewAttachedToWindow(v: View) = Unit
        override fun onViewDetachedFromWindow(v: View) = removeFromWindow()
    }

    /**
     * Sequence hook: lets a sequence take over a deliberate dismissal and move straight on to
     * the next item. Returns true if it took over.
     */
    internal var onDismissRequested: ((MaterialShowcaseView) -> Boolean)? = null

    /* Transition from the previous item in a sequence: the highlight glides from there. */
    private var transitionFromScreen: Point? = null
    private var transitionStartScaleX = 1f
    private var transitionStartScaleY = 1f
    private var transitionProgress = 1f
    private var transitionAnimator: ValueAnimator? = null

    private val showRunnable = Runnable {
        if (!isAttachedToWindow) return@Runnable
        bringTargetOnScreen()
        if (shouldAnimate) {
            // Wait for the first layout so reveal animations know our size.
            doOnLayout { fadeIn() }
        } else {
            visibility = VISIBLE
            onDisplayed()
        }
    }

    init {
        setWillNotDraw(false)
        setOnTouchListener(this)
        visibility = INVISIBLE

        LayoutInflater.from(context).inflate(R.layout.showcase_content, this, true)
        contentBox = findViewById(R.id.content_box)
        builtInContent = contentBox
        titleTextView = findViewById(R.id.tv_title)
        contentTextView = findViewById(R.id.tv_content)
        dismissButton = findViewById(R.id.tv_dismiss)
        skipButton = findViewById(R.id.tv_skip)
        dismissButton.setOnClickListener(this)
        skipButton.setOnClickListener(this)
        dismissButton.announceAsButton()
        skipButton.announceAsButton()
        updateTitleVisibility()
        updateDismissButton()
        updateSkipButton()

        ViewCompat.setOnApplyWindowInsetsListener(this) { _, insets ->
            applyInsets(insets)
            insets
        }
    }

    /* ---------------------------------------------------------------------------------------
     * Drawing
     * ------------------------------------------------------------------------------------- */

    /**
     * Fill the screen with the mask colour, then erase the target's shape to create a
     * "viewport" through to the content underneath.
     *
     * The mask is drawn into an offscreen layer (rather than a full-screen Bitmap) so the
     * CLEAR transfer mode only affects the mask, and we don't allocate a screen-sized bitmap.
     */
    override fun onDraw(canvas: android.graphics.Canvas) {
        super.onDraw(canvas)
        if (!shouldRender) return
        val w = width
        val h = height
        if (w <= 0 || h <= 0) return

        val save = canvas.saveLayer(0f, 0f, w.toFloat(), h.toFloat(), null)
        if (!renderOverNav) {
            // Leave the navigation buttons uncovered. Only relevant when the app draws edge-to-edge
            // with 3-button navigation; the gesture handle has no tappable inset and is dimmed.
            canvas.clipRect(
                navigationButtonInsets.left,
                0,
                w - navigationButtonInsets.right,
                h - navigationButtonInsets.bottom,
            )
        }
        canvas.drawColor(maskColour)
        if (hasTarget()) {
            val hole = currentHolePosition()
            val shapeSave = canvas.save()
            if (transitionProgress < 1f) {
                // Mid-transition: grow/shrink from the previous item's shape size.
                canvas.scale(
                    lerp(transitionStartScaleX, 1f, transitionProgress),
                    lerp(transitionStartScaleY, 1f, transitionProgress),
                    hole.x.toFloat(),
                    hole.y.toFloat(),
                )
            }
            shape?.draw(canvas, eraser, hole.x, hole.y)
            // Re-drawing the shape with a stroke paint outlines it, for built-in and custom shapes alike.
            borderPaint?.let { shape?.draw(canvas, it, hole.x, hole.y) }
            canvas.restoreToCount(shapeSave)
        }
        canvas.restoreToCount(save)
    }

    /* ---------------------------------------------------------------------------------------
     * Attach / detach
     * ------------------------------------------------------------------------------------- */

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        // Register on the *window's* observer; registering before attach puts the listener on
        // a temporary observer that we could never remove it from again.
        viewTreeObserver.addOnGlobalLayoutListener(layoutListener)
        viewTreeObserver.addOnScrollChangedListener(scrollListener)
        ViewCompat.getRootWindowInsets(this)?.let { applyInsets(it) }
        hideSiblingsFromAccessibility()
        showing += this
        ViewCompat.setAccessibilityPaneTitle(
            this,
            if (hasCustomContent) contentBox.contentDescription else titleTextView.text.ifEmpty { contentTextView.text },
        )
    }

    override fun onDetachedFromWindow() {
        viewTreeObserver.removeOnGlobalLayoutListener(layoutListener)
        viewTreeObserver.removeOnScrollChangedListener(scrollListener)
        backCallback?.remove()
        backCallback = null
        removeCallbacks(showRunnable)
        restoreSiblingAccessibility()
        showing -= this
        super.onDetachedFromWindow()

        // Detached without being dismissed or skipped (rotation, user left the Activity...):
        // forget that a single-use showcase was shown so it appears again next time.
        if (!wasDismissed && !wasSkipped && singleUse) {
            prefsManager?.resetShowcase()
        }

        notifyOnDismissed()
    }

    private fun hideSiblingsFromAccessibility() {
        // Over a dialog we live in our own window, so hide the dialog's content instead.
        val parent = (hostRoot ?: parent) as? ViewGroup ?: return
        for (i in 0 until parent.childCount) {
            val sibling = parent.getChildAt(i)
            if (sibling === this) continue
            AccessibilityHider.hide(sibling)
            hiddenForAccessibility += sibling
        }
    }

    private fun restoreSiblingAccessibility() {
        for (view in hiddenForAccessibility) AccessibilityHider.restore(view)
        hiddenForAccessibility.clear()
    }

    /**
     * Reference-counts hiding so overlapping showcases (one animating out while the next comes
     * in) restore the original state whatever order they are removed in.
     */
    private object AccessibilityHider {
        private class Entry(val original: Int, var count: Int)

        private val entries = java.util.WeakHashMap<View, Entry>()

        fun hide(view: View) {
            val entry = entries[view]
            if (entry != null) {
                entry.count++
            } else {
                entries[view] = Entry(view.importantForAccessibility, 1)
                view.importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS
            }
        }

        fun restore(view: View) {
            val entry = entries[view] ?: return
            if (--entry.count == 0) {
                entries.remove(view)
                view.importantForAccessibility = entry.original
            }
        }
    }

    /* ---------------------------------------------------------------------------------------
     * Touch
     * ------------------------------------------------------------------------------------- */

    override fun onTouch(v: View, event: MotionEvent): Boolean {
        if (dismissing) return true

        if (targetTouchable && hasTarget() && targetBoundsInView().contains(event.x.toInt(), event.y.toInt())) {
            // Let the touch through to the target. We won't see the rest of this gesture.
            if (event.actionMasked == MotionEvent.ACTION_DOWN && (dismissOnTargetTouch || dismissOnTouch)) {
                hide()
            }
            return false
        }

        // Dismiss once per gesture, not on every MotionEvent of it.
        if (dismissOnTouch && event.actionMasked == MotionEvent.ACTION_UP) {
            hide()
        }
        return true
    }

    /** Dismiss / skip button clicked. */
    override fun onClick(v: View) {
        when (v.id) {
            R.id.tv_dismiss, R.id.showcase_dismiss -> hide()
            R.id.tv_skip, R.id.showcase_skip -> skip()
        }
    }

    /* ---------------------------------------------------------------------------------------
     * Layout
     * ------------------------------------------------------------------------------------- */

    /**
     * Overrides the automatic placement of the content and uses [gravity] instead.
     * Content margins are reset to zero.
     */
    open fun setGravity(gravity: Int) {
        hasCustomGravity = Gravity.NO_GRAVITY != gravity
        if (hasCustomGravity) {
            contentGravity = gravity
            contentTopMargin = 0
            contentBottomMargin = 0
        }
        applyLayoutParams()
    }

    /**
     * Sets the target to showcase, and works out whether the content should sit above or
     * below it.
     */
    open fun setTarget(target: Target?) {
        this.target = target
        updateDismissButton()
        updateLayout()
    }

    private fun hasTarget(): Boolean = target.let { it != null && it !== Target.NONE }

    /** Where this view sits relative to the window the target's coordinates are in. */
    private fun windowOffset(): Point {
        val host = hostRoot
        if (host != null) {
            // We're in our own panel window over a dialog; targets report dialog-window coordinates.
            val mine = IntArray(2).also { getLocationOnScreen(it) }
            val theirs = IntArray(2).also { host.getLocationOnScreen(it) }
            return Point(mine[0] - theirs[0], mine[1] - theirs[1])
        }
        val location = IntArray(2)
        getLocationInWindow(location)
        return Point(location[0], location[1])
    }

    /** Centre of the highlight as currently drawn, in this view's coordinates. */
    @VisibleForTesting
    internal fun currentHolePosition(): Point {
        val from = transitionFromScreen
        if (from == null || transitionProgress >= 1f) return Point(showcaseX, showcaseY)
        val mine = IntArray(2).also { getLocationOnScreen(it) }
        val fromX = (from.x - mine[0]).toFloat()
        val fromY = (from.y - mine[1]).toFloat()
        return Point(
            lerp(fromX, showcaseX.toFloat(), transitionProgress).toInt(),
            lerp(fromY, showcaseY.toFloat(), transitionProgress).toInt(),
        )
    }

    private fun lerp(from: Float, to: Float, fraction: Float) = from + (to - from) * fraction

    /** Target bounds translated into this view's coordinate space. */
    private fun targetBoundsInView(): Rect {
        val bounds = Rect(target?.getBounds() ?: return Rect())
        val offset = windowOffset()
        bounds.offset(-offset.x, -offset.y)
        return bounds
    }

    private fun updateLayout() {
        val target = target
        if (target != null && hasTarget()) {
            val offset = windowOffset()
            val point = target.getPoint()
            setPosition(point.x - offset.x, point.y - offset.y)

            val bounds = target.getBounds()
            var radius = maxOf(bounds.height(), bounds.width()) / 2
            shape?.let {
                it.updateTarget(target)
                radius = it.getHeight() / 2
            }

            if (!hasCustomGravity) placeContent(radius)
        } else if (!hasCustomGravity) {
            // Nothing to point at: centre the content.
            contentTopMargin = 0
            contentBottomMargin = 0
            contentGravity = Gravity.CENTER_VERTICAL
        }
        applyLayoutParams()
        invalidate()
    }

    /**
     * Puts the content below a target in the top half of the screen and above one in the bottom
     * half. If it doesn't fit there it goes on the other side; if it fits on neither, it is pinned
     * to the screen edge on the roomier side (overlapping the target) so the dismiss button is
     * always reachable.
     */
    private fun placeContent(radius: Int) {
        val height = height
        val spaceBelow = height - paddingBottom - (showcaseY + radius + shapePadding)
        val spaceAbove = (showcaseY - radius - shapePadding) - paddingTop
        val contentHeight = measureContentHeight()
        val preferBelow = showcaseY <= height / 2

        val below = when {
            contentHeight == 0 -> preferBelow // not measurable yet
            preferBelow && contentHeight <= spaceBelow -> true
            !preferBelow && contentHeight <= spaceAbove -> false
            contentHeight <= spaceBelow -> true
            contentHeight <= spaceAbove -> false
            else -> spaceBelow >= spaceAbove
        }
        val fits = contentHeight <= if (below) spaceBelow else spaceAbove

        if (below) {
            contentGravity = if (fits) Gravity.TOP else Gravity.BOTTOM
            contentTopMargin = if (fits) (height - paddingTop - paddingBottom - spaceBelow).coerceAtLeast(0) else 0
            contentBottomMargin = 0
        } else {
            contentGravity = if (fits) Gravity.BOTTOM else Gravity.TOP
            contentTopMargin = 0
            contentBottomMargin = if (fits) (height - paddingTop - paddingBottom - spaceAbove).coerceAtLeast(0) else 0
        }
    }

    private fun measureContentHeight(): Int {
        val available = width - paddingLeft - paddingRight
        if (available <= 0) return 0
        contentBox.measure(
            MeasureSpec.makeMeasureSpec(available, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
        )
        return contentBox.measuredHeight
    }

    private fun applyLayoutParams() {
        val lp = contentBox.layoutParams as? LayoutParams ?: return
        var changed = false
        if (lp.bottomMargin != contentBottomMargin) {
            lp.bottomMargin = contentBottomMargin
            changed = true
        }
        if (lp.topMargin != contentTopMargin) {
            lp.topMargin = contentTopMargin
            changed = true
        }
        if (lp.gravity != contentGravity) {
            lp.gravity = contentGravity
            changed = true
        }
        // Only apply when something changed, otherwise we'd loop forever in layout.
        if (changed) contentBox.layoutParams = lp
        updateToolTip()
    }

    @VisibleForTesting
    internal fun applyInsets(insets: WindowInsetsCompat) {
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
        val nav = insets.getInsets(WindowInsetsCompat.Type.navigationBars())
        val tappable = insets.getInsets(WindowInsetsCompat.Type.tappableElement())
        // Navigation buttons = the part of the navigation bar that takes taps (none for gestures).
        val navButtons = Insets.min(nav, tappable)
        if (bars == systemInsets && navButtons == navigationButtonInsets) return
        systemInsets = bars
        navigationButtonInsets = navButtons
        // Keep the text and buttons clear of the status bar, navigation bar and cut-outs.
        setPadding(bars.left, bars.top, bars.right, bars.bottom)
        updateLayout()
    }

    private fun updateToolTip() {
        val toolTip = toolTip ?: return
        // Only once the showcase is visible; before that the tooltip has no anchor and its enter
        // animation would play behind an invisible overlay.
        if (!displayed || !isAttachedToWindow) return
        if (!toolTipShown) {
            toolTipShown = true
            val shapeDiameter = (shape?.getTotalRadius() ?: 0) * 2
            val targetHeight = target?.getBounds()?.height() ?: 0
            toolTip.show((shapeDiameter - targetHeight) / 2 + tooltipMargin)
        }
        toolTip.position(
            if (contentGravity == Gravity.BOTTOM) ShowcaseTooltip.Position.TOP else ShowcaseTooltip.Position.BOTTOM,
        )
    }

    /* ---------------------------------------------------------------------------------------
     * Setters
     * ------------------------------------------------------------------------------------- */

    internal fun setPosition(x: Int, y: Int) {
        showcaseX = x
        showcaseY = y
        invalidate()
    }

    private fun setTitleText(text: CharSequence?) {
        titleTextView.text = text
        updateTitleVisibility()
    }

    private fun updateTitleVisibility() {
        titleTextView.visibility = if (titleTextView.text.isNullOrEmpty()) GONE else VISIBLE
    }

    private fun setContentText(text: CharSequence?) {
        contentTextView.text = text
    }

    private fun setToolTip(toolTip: ShowcaseTooltip?) {
        this.toolTip = toolTip
    }

    private fun setIsSequence(isSequence: Boolean) {
        this.isSequence = isSequence
    }

    private fun setDismissText(text: CharSequence?) {
        dismissButton.text = text
        updateDismissButton()
    }

    private fun setSkipText(text: CharSequence?) {
        skipButton.text = text
        updateSkipButton()
    }

    private fun setDismissStyle(style: Typeface?) {
        dismissButton.typeface = style
    }

    private fun setSkipStyle(style: Typeface?) {
        skipButton.typeface = style
    }

    private fun setTitleTextColor(colour: Int) = titleTextView.setTextColor(colour)

    private fun setContentTextColor(colour: Int) = contentTextView.setTextColor(colour)

    private fun setDismissTextColor(colour: Int) = dismissButton.setTextColor(colour)

    private fun setSkipTextColor(colour: Int) = skipButton.setTextColor(colour)

    private fun setTitleStyle(style: Typeface?) {
        titleTextView.typeface = style
    }

    private fun setContentStyle(style: Typeface?) {
        contentTextView.typeface = style
    }

    private fun setTitleTextSize(unit: Int, size: Float) = titleTextView.setTextSize(unit, size)

    private fun setContentTextSize(unit: Int, size: Float) = contentTextView.setTextSize(unit, size)

    private fun setDismissTextSize(unit: Int, size: Float) = dismissButton.setTextSize(unit, size)

    private fun setSkipTextSize(unit: Int, size: Float) = skipButton.setTextSize(unit, size)

    private fun setShapeBorder(colour: Int, width: Int) {
        borderPaint = if (width <= 0) null else Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = colour
            style = Paint.Style.STROKE
            strokeWidth = width.toFloat()
        }
        invalidate()
    }

    private fun setDismissBackground(background: Drawable?) = dismissButton.setButtonBackground(background)

    private fun setSkipBackground(background: Drawable?) = skipButton.setButtonBackground(background)

    private fun setDismissButtonColor(colour: Int) = dismissButton.setButtonBackground(filledButton(colour))

    private fun setSkipButtonColor(colour: Int) = skipButton.setButtonBackground(filledButton(colour))

    /** A rounded, filled button background with a ripple, in [colour]. */
    private fun filledButton(colour: Int): Drawable {
        val radius = 4 * resources.displayMetrics.density
        val shape = GradientDrawable().apply {
            cornerRadius = radius
            setColor(colour)
        }
        val mask = GradientDrawable().apply {
            cornerRadius = radius
            setColor(Color.WHITE)
        }
        return RippleDrawable(ColorStateList.valueOf(0x40FFFFFF), shape, mask)
    }

    /**
     * Applies [background] with button-like padding; null restores the default plain text button
     * (the theme's selectable item background).
     */
    private fun TextView.setButtonBackground(background: Drawable?) {
        val density = resources.displayMetrics.density
        val horizontal = ((if (background != null) 16 else 5) * density).toInt()
        val vertical = (10 * density).toInt()
        this.background = background ?: android.util.TypedValue().let { tv ->
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, tv, true)
            androidx.core.content.ContextCompat.getDrawable(context, tv.resourceId)
        }
        setPaddingRelative(horizontal, vertical, horizontal, vertical)
    }

    /**
     * Replaces the built-in title, text and buttons with [content]. Views in it with the ids
     * `showcase_dismiss` / `showcase_skip` dismiss / skip the showcase when clicked.
     */
    private fun setCustomContent(content: View?) {
        (contentBox.parent as? ViewGroup)?.removeView(contentBox)
        if (content == null) {
            hasCustomContent = false
            contentBox = builtInContent
        } else {
            hasCustomContent = true
            (content.parent as? ViewGroup)?.removeView(content)
            contentBox = content
            content.findViewById<View>(R.id.showcase_dismiss)?.setOnClickListener(this)
            content.findViewById<View>(R.id.showcase_skip)?.setOnClickListener(this)
        }
        val width = content?.layoutParams?.width ?: LayoutParams.MATCH_PARENT
        addView(contentBox, LayoutParams(width, LayoutParams.WRAP_CONTENT))
        updateLayout()
    }

    private fun setCustomContent(layoutRes: Int) =
        setCustomContent(LayoutInflater.from(context).inflate(layoutRes, this, false))

    private fun setScrollTargetIntoView(scroll: Boolean) {
        scrollTargetIntoView = scroll
    }

    /** If asked to, scrolls the target's scrolling parents so the target is fully on screen. */
    private fun bringTargetOnScreen() {
        if (!scrollTargetIntoView) return
        val view = (target as? ViewTarget)?.view ?: return
        if (view.width == 0 && view.height == 0) return
        view.requestRectangleOnScreen(Rect(0, 0, view.width, view.height), true)
        updateLayout()
    }

    private fun setDismissOnBackPress(dismissOnBackPress: Boolean) {
        this.dismissOnBackPress = dismissOnBackPress
        // Fallback for Activities without an OnBackPressedDispatcher: take key focus.
        isFocusableInTouchMode = dismissOnBackPress
    }

    private fun setShapePadding(padding: Int) {
        shapePadding = padding
        shape?.setPadding(padding)
    }

    private fun setTooltipMargin(margin: Int) {
        tooltipMargin = margin
    }

    private fun setDismissOnTouch(dismissOnTouch: Boolean) {
        this.dismissOnTouch = dismissOnTouch
    }

    private fun setMaskColour(colour: Int) {
        maskColour = colour
        invalidate()
    }

    private fun setDelay(delayInMillis: Long) {
        this.delayInMillis = delayInMillis
    }

    private fun setFadeDuration(fadeDurationInMillis: Long) {
        this.fadeDurationInMillis = fadeDurationInMillis
    }

    private fun setTargetTouchable(targetTouchable: Boolean) {
        this.targetTouchable = targetTouchable
    }

    private fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean) {
        this.dismissOnTargetTouch = dismissOnTargetTouch
    }

    private fun setUseFadeAnimation(useFadeAnimation: Boolean) {
        this.useFadeAnimation = useFadeAnimation
    }

    private fun setRenderOverNavigationBar(renderOverNav: Boolean) {
        this.renderOverNav = renderOverNav
        invalidate()
    }

    open fun addShowcaseListener(showcaseListener: IShowcaseListener?) {
        if (showcaseListener != null) listeners += showcaseListener
    }

    open fun removeShowcaseListener(showcaseListener: IShowcaseListener?) {
        listeners.remove(showcaseListener)
    }

    @Deprecated(
        "A MaterialShowcaseSequence is never registered as a listener, so this did nothing.",
        level = DeprecationLevel.WARNING,
    )
    open fun removeShowcaseListener(showcaseListener: MaterialShowcaseSequence?) = Unit

    internal fun setDetachedListener(detachedListener: IDetachedListener?) {
        this.detachedListener = detachedListener
    }

    /** Sets the highlight shape; null means the Builder's default shape. */
    open fun setShape(shape: Shape?) {
        this.shape = shape
        shape?.setPadding(shapePadding)
    }

    /** Sets how the showcase animates in and out; null restores the default. */
    open fun setAnimationFactory(animationFactory: IAnimationFactory?) {
        this.animationFactory = animationFactory ?: CircularRevealAnimationFactory()
        hasCustomAnimationFactory = animationFactory != null
    }

    /** Applies the values that were explicitly set on [config]. */
    open fun setConfig(config: ShowcaseConfig?) {
        if (config == null) return
        if (config.delay > -1) setDelay(config.delay)
        if (config.fadeDuration > 0) setFadeDuration(config.fadeDuration)
        if (config.isTitleTextColorSet) setTitleTextColor(config.titleTextColor)
        if (config.isContentTextColorSet) setContentTextColor(config.contentTextColor)
        if (config.isDismissTextColorSet) setDismissTextColor(config.dismissTextColor)
        if (config.isMaskColorSet) setMaskColour(config.maskColor)
        config.dismissTextStyle?.let { setDismissStyle(it) }
        config.shape?.let { setShape(it) }
        if (config.shapePadding > -1) setShapePadding(config.shapePadding)
        config.renderOverNavigationBar?.let { setRenderOverNavigationBar(it) }
        config.titleTextStyle?.let { setTitleStyle(it) }
        config.contentTextStyle?.let { setContentStyle(it) }
        config.titleTextSize?.let { setTitleTextSize(it.unit, it.size) }
        config.contentTextSize?.let { setContentTextSize(it.unit, it.size) }
        config.dismissTextSize?.let { setDismissTextSize(it.unit, it.size) }
        config.dismissOnBackPress?.let { setDismissOnBackPress(it) }
        config.dismissButtonColor?.let { setDismissButtonColor(it) }
        config.skipButtonColor?.let { setSkipButtonColor(it) }
        config.shapeBorder?.let { setShapeBorder(it.colour, it.width) }
        config.scrollTargetIntoView?.let { setScrollTargetIntoView(it) }
    }

    private fun updateDismissButton() {
        dismissButton.visibility = if (dismissButton.text.isNullOrEmpty()) GONE else VISIBLE
    }

    private fun updateSkipButton() {
        skipButton.visibility = if (skipButton.text.isNullOrEmpty()) GONE else VISIBLE
    }

    /** True if this is a single-use showcase that has already been shown. */
    open fun hasFired(): Boolean = prefsManager?.hasFired() ?: false

    /**
     * True if the showcase was skipped (the skip button or, with dismiss-on-back-press, the back
     * button) rather than dismissed. Useful in [IShowcaseListener.onShowcaseDismissed].
     */
    open fun wasSkipped(): Boolean = wasSkipped

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        if (dismissOnBackPress && backCallback == null && event.keyCode == KeyEvent.KEYCODE_BACK) {
            if (event.action == KeyEvent.ACTION_UP && !event.isCanceled) skip()
            return true
        }
        return super.dispatchKeyEvent(event)
    }

    private fun registerBackHandler(activity: Activity, dialog: Dialog?) {
        if (!dismissOnBackPress) return
        if (dialog != null) {
            // Over a dialog the dialog keeps key focus, so this needs an AndroidX dialog
            // (AppCompatDialog, ComponentDialog) to intercept back presses.
            if (dialog is ComponentDialog) {
                val callback = object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() = skip()
                }
                dialog.onBackPressedDispatcher.addCallback(callback)
                backCallback = callback
            }
            return
        }
        if (activity is ComponentActivity) {
            val callback = object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() = skip()
            }
            activity.onBackPressedDispatcher.addCallback(callback)
            backCallback = callback
        } else {
            requestFocus()
        }
    }

    private fun singleUse(showcaseID: String) {
        singleUse = true
        prefsManager = PrefsManager(context, showcaseID)
    }

    /* ---------------------------------------------------------------------------------------
     * Show / hide
     * ------------------------------------------------------------------------------------- */

    /**
     * Shows the showcase over [activity]. Returns false if nothing was shown, e.g. because
     * it is single-use and has been shown before, or it is already showing.
     */
    open fun show(activity: Activity?): Boolean =
        showInternal(requireNotNull(activity) { "show() needs an Activity" }, null, null)

    /**
     * Shows the showcase over [dialog], which must already be showing. The showcase covers the
     * whole screen, above the dialog, and goes away if the dialog is dismissed.
     */
    open fun show(dialog: Dialog?): Boolean {
        requireNotNull(dialog) { "show() needs a Dialog" }
        val activity = dialog.ownerActivity ?: findActivity(dialog.context)
            ?: throw IllegalArgumentException("The dialog must belong to an Activity")
        return showInternal(activity, dialog, null)
    }

    /**
     * Shows the showcase in [activity], or over [dialog] if given. With [previous] (the item
     * before this one in a sequence) it appears immediately and its highlight glides from there.
     */
    internal fun showInternal(activity: Activity, dialog: Dialog?, previous: MaterialShowcaseView?): Boolean {
        check(Looper.myLooper() == Looper.getMainLooper()) {
            "MaterialShowcaseView.show() must be called on the main thread"
        }
        // Globally disabled (e.g. in UI tests): show nothing and record nothing.
        if (!showcasesEnabled) return false
        if (parent != null) return false
        val tooltipAnchor = toolTip?.let {
            (target as? ViewTarget)?.view
                ?: throw IllegalStateException("A tooltip requires a target of type ${ViewTarget::class.java.name}")
        }

        // Single-use and already shown: do nothing.
        prefsManager?.let {
            if (singleUse) {
                if (it.hasFired()) return false
                it.setFired()
            }
        }

        wasDismissed = false
        wasSkipped = false
        dismissing = false
        notifiedDismissed = false
        toolTipShown = false
        displayed = false

        hostActivity = activity
        if (dialog != null) {
            attachOverDialog(dialog)
        } else {
            (activity.window.decorView as ViewGroup).addView(this)
        }
        shouldRender = true

        if (tooltipAnchor != null) toolTip?.configureTarget(this, tooltipAnchor)
        registerBackHandler(activity, dialog)

        if (previous != null) startTransitionFrom(previous) else postDelayed(showRunnable, delayInMillis)
        updateDismissButton()
        return true
    }

    /**
     * Adds this view in a full-screen panel window attached to [dialog]'s window. A dialog's
     * window attaches on the frame after `show()`, so if it isn't attached yet we wait for it.
     */
    private fun attachOverDialog(dialog: Dialog) {
        val window = dialog.window ?: throw IllegalStateException("The dialog has no window")
        check(dialog.isShowing) { "Show the dialog before showing a showcase over it" }
        val root = window.decorView

        val container = FrameLayout(context)
        container.addView(this, LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT))
        hostRoot = root
        panel = container
        if (root.isAttachedToWindow) {
            addPanelWindow(window, root, container)
        } else {
            val pending = object : OnAttachStateChangeListener {
                override fun onViewAttachedToWindow(v: View) {
                    root.removeOnAttachStateChangeListener(this)
                    pendingPanelAttach = null
                    if (panel === container) addPanelWindow(window, root, container)
                }

                override fun onViewDetachedFromWindow(v: View) = Unit
            }
            pendingPanelAttach = pending
            root.addOnAttachStateChangeListener(pending)
        }
    }

    private var pendingPanelAttach: OnAttachStateChangeListener? = null

    private fun addPanelWindow(window: android.view.Window, root: View, container: FrameLayout) {
        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT,
        )
        lp.token = root.windowToken
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) lp.setFitInsetsTypes(0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        val windowManager = window.windowManager
        panelWindowManager = windowManager
        windowManager.addView(container, lp)
        root.addOnAttachStateChangeListener(hostDetachListener)
    }

    /** Appear immediately, with the highlight gliding from [previous]'s and the text fading in. */
    private fun startTransitionFrom(previous: MaterialShowcaseView) {
        bringTargetOnScreen()
        if (previous.hasTarget() && hasTarget()) {
            val screen = IntArray(2).also { previous.getLocationOnScreen(it) }
            val hole = previous.currentHolePosition()
            transitionFromScreen = Point(screen[0] + hole.x, screen[1] + hole.y)
            transitionStartScaleX = sizeRatio(previous.shape?.getWidth(), shape?.getWidth())
            transitionStartScaleY = sizeRatio(previous.shape?.getHeight(), shape?.getHeight())
        } else {
            transitionFromScreen = null
            transitionStartScaleX = 1f
            transitionStartScaleY = 1f
        }
        transitionProgress = 0f
        contentBox.alpha = 0f
        visibility = VISIBLE
        doOnLayout {
            if (!isAttachedToWindow || dismissing) return@doOnLayout
            transitionAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
                duration = fadeDurationInMillis
                interpolator = AccelerateDecelerateInterpolator()
                addUpdateListener {
                    transitionProgress = it.animatedValue as Float
                    contentBox.alpha = transitionProgress
                    invalidate()
                }
                doOnEndOrCancel {
                    transitionProgress = 1f
                    contentBox.alpha = 1f
                    transitionFromScreen = null
                    invalidate()
                    if (isAttachedToWindow && !dismissing) onDisplayed()
                }
                start()
            }
        }
    }

    private fun sizeRatio(from: Int?, to: Int?): Float {
        if (from == null || to == null || from <= 0 || to <= 0) return 1f
        return (from.toFloat() / to).coerceIn(0.05f, 20f)
    }

    /** Dismisses the showcase (as if "GOT IT" was tapped). Safe to call more than once. */
    open fun hide() {
        if (dismissing) return
        // Tells onDetachedFromWindow that this was deliberate.
        wasDismissed = true
        dismiss()
    }

    /** Skips the showcase; in a sequence this ends the whole sequence. Safe to call more than once. */
    open fun skip() {
        if (dismissing) return
        wasSkipped = true
        dismiss()
    }

    private fun dismiss() {
        dismissing = true
        removeCallbacks(showRunnable)
        // In a sequence, a deliberate dismissal can hand straight over to the next item.
        if (wasDismissed && onDismissRequested?.invoke(this) == true) return
        if (shouldAnimate && isAttachedToWindow && visibility == VISIBLE) {
            animateOut()
        } else {
            removeFromWindow()
        }
    }

    open fun fadeIn() {
        if (!isAttachedToWindow) return
        visibility = INVISIBLE
        animationFactory.animateInView(this, revealPoint(), fadeDurationInMillis) {
            visibility = VISIBLE
            onDisplayed()
        }
    }

    open fun animateOut() {
        if (!isAttachedToWindow) {
            removeFromWindow()
            return
        }
        animationFactory.animateOutView(this, revealPoint(), fadeDurationInMillis) {
            visibility = INVISIBLE
            removeFromWindow()
        }
    }

    /** Point to reveal from / collapse into: the target, or the centre if there isn't one. */
    private fun revealPoint(): Point =
        if (hasTarget()) Point(showcaseX, showcaseY) else Point(width / 2, height / 2)

    open fun removeFromWindow() {
        removeCallbacks(showRunnable)
        transitionAnimator?.cancel()
        transitionAnimator = null
        val container = panel
        if (container != null) {
            panel = null
            hostRoot?.removeOnAttachStateChangeListener(hostDetachListener)
            pendingPanelAttach?.let { hostRoot?.removeOnAttachStateChangeListener(it) }
            pendingPanelAttach = null
            if (container.isAttachedToWindow) panelWindowManager?.removeViewImmediate(container)
            panelWindowManager = null
            hostRoot = null
            container.removeView(this)
        } else {
            (parent as? ViewGroup)?.removeView(this)
        }
    }

    private fun onDisplayed() {
        displayed = true
        updateToolTip()
        for (listener in listeners.toList()) listener.onShowcaseDisplayed(this)
    }

    private fun notifyOnDismissed() {
        if (notifiedDismissed) return
        notifiedDismissed = true
        for (listener in listeners.toList()) listener.onShowcaseDismissed(this)
        // Internal listener used by sequences to track progress.
        detachedListener?.onShowcaseDetached(this, wasDismissed, wasSkipped)
    }

    open fun resetSingleUse() {
        if (singleUse) prefsManager?.resetShowcase()
    }

    @Deprecated("Navigation bar size is now taken from window insets.")
    open fun getSoftButtonsBarSizePort(): Int {
        val insets = ViewCompat.getRootWindowInsets(this) ?: return 0
        return insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
    }

    private fun TextView.announceAsButton() {
        ViewCompat.setAccessibilityDelegate(this, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                info.className = Button::class.java.name
            }
        })
    }

    /**
     * Fluent API for configuring and showing a showcase.
     */
    open class Builder(activity: Activity?) {

        private val activity: Activity = requireNotNull(activity) { "MaterialShowcaseView.Builder needs an Activity" }

        internal val showcaseView = MaterialShowcaseView(this.activity)

        private var fullWidth = false
        private var shapeType = CIRCLE_SHAPE
        private var cornerRadius = -1

        /** Places the content with [gravity] instead of positioning it automatically. */
        open fun setGravity(gravity: Int): Builder = apply { showcaseView.setGravity(gravity) }

        /** Sets the view to showcase. Without a target the whole screen is masked. */
        open fun setTarget(target: View?): Builder =
            apply { showcaseView.setTarget(target?.let { ViewTarget(it) }) }

        open fun setSequence(isSequence: Boolean?): Builder = apply { showcaseView.setIsSequence(isSequence == true) }

        open fun setDismissText(resId: Int): Builder = setDismissText(activity.getString(resId))

        open fun setDismissText(dismissText: CharSequence?): Builder = apply { showcaseView.setDismissText(dismissText) }

        open fun setDismissStyle(dismissStyle: Typeface?): Builder = apply { showcaseView.setDismissStyle(dismissStyle) }

        open fun setSkipText(resId: Int): Builder = setSkipText(activity.getString(resId))

        open fun setSkipText(skipText: CharSequence?): Builder = apply { showcaseView.setSkipText(skipText) }

        open fun setSkipStyle(skipStyle: Typeface?): Builder = apply { showcaseView.setSkipStyle(skipStyle) }

        open fun setContentText(resId: Int): Builder = setContentText(activity.getString(resId))

        open fun setContentText(text: CharSequence?): Builder = apply { showcaseView.setContentText(text) }

        open fun setTitleText(resId: Int): Builder = setTitleText(activity.getString(resId))

        open fun setTitleText(text: CharSequence?): Builder = apply { showcaseView.setTitleText(text) }

        /** Shows [toolTip] pointing at the target. Requires a [View] target. */
        open fun setToolTip(toolTip: ShowcaseTooltip?): Builder = apply { showcaseView.setToolTip(toolTip) }

        /** Whether the target can be touched while the showcase is visible. False by default. */
        open fun setTargetTouchable(targetTouchable: Boolean): Builder =
            apply { showcaseView.setTargetTouchable(targetTouchable) }

        /** Whether touching a touchable target dismisses the showcase. True by default. */
        open fun setDismissOnTargetTouch(dismissOnTargetTouch: Boolean): Builder =
            apply { showcaseView.setDismissOnTargetTouch(dismissOnTargetTouch) }

        /** Whether tapping anywhere dismisses the showcase. False by default. */
        open fun setDismissOnTouch(dismissOnTouch: Boolean): Builder =
            apply { showcaseView.setDismissOnTouch(dismissOnTouch) }

        open fun setMaskColour(maskColour: Int): Builder = apply { showcaseView.setMaskColour(maskColour) }

        open fun setTitleTextColor(textColour: Int): Builder = apply { showcaseView.setTitleTextColor(textColour) }

        open fun setContentTextColor(textColour: Int): Builder = apply { showcaseView.setContentTextColor(textColour) }

        open fun setDismissTextColor(textColour: Int): Builder = apply { showcaseView.setDismissTextColor(textColour) }

        open fun setSkipTextColor(textColour: Int): Builder = apply { showcaseView.setSkipTextColor(textColour) }

        /** Shows the dismiss text as a filled, rounded button in [colour]. */
        open fun setDismissButtonColor(colour: Int): Builder = apply { showcaseView.setDismissButtonColor(colour) }

        /** Shows the skip text as a filled, rounded button in [colour]. */
        open fun setSkipButtonColor(colour: Int): Builder = apply { showcaseView.setSkipButtonColor(colour) }

        /** Any background for the dismiss button, e.g. a drawable from your theme. Null for plain text. */
        open fun setDismissBackground(background: Drawable?): Builder =
            apply { showcaseView.setDismissBackground(background) }

        /** Any background for the skip button. Null for plain text. */
        open fun setSkipBackground(background: Drawable?): Builder = apply { showcaseView.setSkipBackground(background) }

        /** Draws an outline of [width] pixels in [colour] around the highlighted shape. */
        open fun setShapeBorder(colour: Int, width: Int): Builder = apply { showcaseView.setShapeBorder(colour, width) }

        /**
         * Uses [content] in place of the built-in title, text and buttons. The showcase still
         * positions it beside the target. Give a view the id `showcase_dismiss` or
         * `showcase_skip` (`@id/showcase_dismiss` in XML) to make it dismiss or skip; or call
         * `hide()` / `skip()` on the showcase yourself. Null restores the built-in content.
         */
        open fun setContentView(content: View?): Builder = apply { showcaseView.setCustomContent(content) }

        /** Like [setContentView], inflating [layoutRes]. */
        open fun setContentLayout(layoutRes: Int): Builder = apply { showcaseView.setCustomContent(layoutRes) }

        /**
         * Scroll the target's scrolling parents (ScrollView, NestedScrollView, RecyclerView...)
         * so it is fully on screen before the showcase appears. False by default.
         */
        open fun setScrollTargetIntoView(scroll: Boolean): Builder = apply { showcaseView.setScrollTargetIntoView(scroll) }

        /** Typeface for the title. */
        open fun setTitleStyle(titleStyle: Typeface?): Builder = apply { showcaseView.setTitleStyle(titleStyle) }

        /** Typeface for the content text. */
        open fun setContentStyle(contentStyle: Typeface?): Builder = apply { showcaseView.setContentStyle(contentStyle) }

        /** Title text size, e.g. `setTitleTextSize(TypedValue.COMPLEX_UNIT_SP, 24f)`. */
        open fun setTitleTextSize(unit: Int, size: Float): Builder = apply { showcaseView.setTitleTextSize(unit, size) }

        open fun setContentTextSize(unit: Int, size: Float): Builder =
            apply { showcaseView.setContentTextSize(unit, size) }

        open fun setDismissTextSize(unit: Int, size: Float): Builder =
            apply { showcaseView.setDismissTextSize(unit, size) }

        open fun setSkipTextSize(unit: Int, size: Float): Builder = apply { showcaseView.setSkipTextSize(unit, size) }

        /**
         * Whether the back button closes the showcase. It counts as a skip, so in a sequence
         * it ends the whole sequence. False by default.
         */
        open fun setDismissOnBackPress(dismissOnBackPress: Boolean): Builder =
            apply { showcaseView.setDismissOnBackPress(dismissOnBackPress) }

        open fun setDelay(delayInMillis: Int): Builder = apply { showcaseView.setDelay(delayInMillis.toLong()) }

        open fun setFadeDuration(fadeDurationInMillis: Int): Builder =
            apply { showcaseView.setFadeDuration(fadeDurationInMillis.toLong()) }

        open fun setListener(listener: IShowcaseListener?): Builder = apply { showcaseView.addShowcaseListener(listener) }

        /** Only show this showcase once, ever, for the given ID. */
        open fun singleUse(showcaseID: String?): Builder =
            apply { showcaseView.singleUse(requireNotNull(showcaseID) { "singleUse() needs an ID" }) }

        open fun setShape(shape: Shape?): Builder = apply { showcaseView.setShape(shape) }

        open fun withCircleShape(): Builder = apply { shapeType = CIRCLE_SHAPE }

        open fun withOvalShape(): Builder = apply { shapeType = OVAL_SHAPE }

        open fun withoutShape(): Builder = apply { shapeType = NO_SHAPE }

        open fun setShapePadding(padding: Int): Builder = apply { showcaseView.setShapePadding(padding) }

        open fun setTooltipMargin(margin: Int): Builder = apply { showcaseView.setTooltipMargin(margin) }

        open fun withRectangleShape(): Builder = withRectangleShape(false)

        open fun withRectangleShape(fullWidth: Boolean): Builder = apply {
            shapeType = RECTANGLE_SHAPE
            this.fullWidth = fullWidth
        }

        /** Also dim the navigation bar area when the app draws edge-to-edge. */
        open fun renderOverNavigationBar(): Builder = apply { showcaseView.setRenderOverNavigationBar(true) }

        /** A rectangle with rounded corners around the target. */
        open fun withRoundedRectangleShape(): Builder = withRoundedRectangleShape(-1)

        /** A rectangle with rounded corners of [cornerRadius] pixels around the target. */
        open fun withRoundedRectangleShape(cornerRadius: Int): Builder = apply {
            shapeType = ROUNDED_RECTANGLE_SHAPE
            this.cornerRadius = cornerRadius
        }

        open fun useFadeAnimation(): Builder = apply { showcaseView.setUseFadeAnimation(true) }

        open fun build(): MaterialShowcaseView {
            val view = showcaseView
            if (view.shape == null) {
                val target = view.target ?: Target.NONE
                view.setShape(
                    when (shapeType) {
                        RECTANGLE_SHAPE -> RectangleShape(target.getBounds(), fullWidth)
                        ROUNDED_RECTANGLE_SHAPE -> RoundedRectangleShape(
                            target.getBounds(),
                            false,
                            if (cornerRadius >= 0) cornerRadius else (DEFAULT_CORNER_RADIUS_DP * activity.resources.displayMetrics.density).toInt(),
                        )
                        NO_SHAPE -> NoShape()
                        OVAL_SHAPE -> OvalShape(target)
                        else -> CircleShape(target)
                    },
                )
            }
            if (!view.hasCustomAnimationFactory) {
                view.animationFactory =
                    if (view.useFadeAnimation) FadeAnimationFactory() else CircularRevealAnimationFactory()
            }
            return view
        }

        open fun show(): MaterialShowcaseView {
            build().show(activity)
            return showcaseView
        }

        private companion object {
            const val CIRCLE_SHAPE = 0
            const val RECTANGLE_SHAPE = 1
            const val NO_SHAPE = 2
            const val OVAL_SHAPE = 3
            const val ROUNDED_RECTANGLE_SHAPE = 4
            const val DEFAULT_CORNER_RADIUS_DP = 12
        }
    }

    companion object {
        const val DEFAULT_SHAPE_PADDING = 10
        const val DEFAULT_TOOLTIP_MARGIN = 10
        private const val DEFAULT_DELAY = 0L
        private const val DEFAULT_FADE_TIME = 300L

        /** Forget that the single-use showcase or sequence with [showcaseID] has been shown. */
        @JvmStatic
        fun resetSingleUse(context: Context?, showcaseID: String?) =
            PrefsManager.resetShowcase(requireNotNull(context), showcaseID)

        /** Forget every single-use showcase and sequence. */
        @JvmStatic
        fun resetAll(context: Context?) = PrefsManager.resetAll(requireNotNull(context))

        /**
         * True if the single-use showcase or sequence with [showcaseID] has already been shown,
         * so you can skip building it altogether.
         */
        @Volatile
        private var showcasesEnabled = true

        /**
         * Turns every showcase and sequence on or off, for example `false` in UI tests so
         * overlays don't get in the way. While disabled nothing is shown or recorded as shown.
         */
        @JvmStatic
        fun setShowcasesEnabled(enabled: Boolean) {
            showcasesEnabled = enabled
        }

        @JvmStatic
        fun areShowcasesEnabled(): Boolean = showcasesEnabled

        /** Showcases currently attached to a window. */
        private val showing: MutableSet<MaterialShowcaseView> =
            java.util.Collections.newSetFromMap(java.util.WeakHashMap())

        /**
         * True if a showcase (including one in a running sequence, or one over a dialog) is
         * currently on [activity].
         */
        @JvmStatic
        fun isShowing(activity: Activity?): Boolean =
            activity != null && showing.any { it.hostActivity === activity }

        private fun findActivity(context: Context?): Activity? {
            var c = context
            while (c is ContextWrapper) {
                if (c is Activity) return c
                c = c.baseContext
            }
            return null
        }

        @JvmStatic
        fun hasFired(context: Context?, showcaseID: String?): Boolean =
            PrefsManager(context, requireNotNull(showcaseID)).hasFired()
    }
}
