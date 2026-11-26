package uk.co.deanwild.materialshowcaseview

import android.app.Activity
import android.view.View
import uk.co.deanwild.materialshowcaseview.compat.SequenceItemCallbacks
import java.util.LinkedList
import java.util.Queue

/**
 * Shows a series of [MaterialShowcaseView]s one after another.
 *
 * With a sequence ID (single-use) the sequence remembers how far the user got, resumes
 * from there next time, and never shows again once finished or skipped.
 */
open class MaterialShowcaseSequence(activity: Activity?) : IDetachedListener {

    private val activity: Activity = requireNotNull(activity) { "MaterialShowcaseSequence needs an Activity" }

    internal var prefsManager: PrefsManager? = null
        private set
    private val showcaseQueue: Queue<MaterialShowcaseView> = LinkedList()
    private var singleUse = false
    private var config: ShowcaseConfig? = null
    private var sequencePosition = 0

    private var onItemShownListener: OnSequenceItemShownListener? = null
    private var onItemDismissedListener: OnSequenceItemDismissedListener? = null
    private var onSkippedListener: OnSequenceSkippedListener? = null
    private var onFinishedListener: OnSequenceFinishedListener? = null

    constructor(activity: Activity?, sequenceID: String?) : this(activity) {
        singleUse(sequenceID)
    }

    open fun addSequenceItem(targetView: View?, content: String?, dismissText: String?): MaterialShowcaseSequence =
        addSequenceItem(targetView, "", content, dismissText)

    open fun addSequenceItem(
        targetView: View?,
        title: String?,
        content: String?,
        dismissText: String?,
    ): MaterialShowcaseSequence {
        val sequenceItem = MaterialShowcaseView.Builder(activity)
            .setTarget(targetView)
            .setTitleText(title)
            .setDismissText(dismissText)
            .setContentText(content)
            .setSequence(true)
            .build()
        return addSequenceItem(sequenceItem)
    }

    /**
     * Adds a showcase to the end of the sequence. If a [ShowcaseConfig] has been set it is
     * applied now, so call [setConfig] before adding items.
     */
    open fun addSequenceItem(sequenceItem: MaterialShowcaseView?): MaterialShowcaseSequence {
        requireNotNull(sequenceItem) { "addSequenceItem() needs a MaterialShowcaseView" }
        config?.let { sequenceItem.setConfig(it) }
        showcaseQueue.add(sequenceItem)
        return this
    }

    open fun singleUse(sequenceID: String?): MaterialShowcaseSequence {
        singleUse = true
        prefsManager = PrefsManager(activity, requireNotNull(sequenceID) { "singleUse() needs an ID" })
        return this
    }

    open fun setOnItemShownListener(listener: OnSequenceItemShownListener?) {
        onItemShownListener = listener
    }

    open fun setOnItemDismissedListener(listener: OnSequenceItemDismissedListener?) {
        onItemDismissedListener = listener
    }

    /** Called when the user skips the sequence, with the item and position they skipped at. */
    open fun setOnSkippedListener(listener: OnSequenceSkippedListener?) {
        onSkippedListener = listener
    }

    /**
     * Called once when the sequence ends, either because the last item was dismissed or because
     * it was skipped. Not called if the user leaves part-way through.
     */
    open fun setOnSequenceFinishedListener(listener: OnSequenceFinishedListener?) {
        onFinishedListener = listener
    }

    /** True if this is a single-use sequence that has been finished or skipped. */
    open fun hasFired(): Boolean = prefsManager?.getSequenceStatus() == PrefsManager.SEQUENCE_FINISHED

    open fun start() {
        val prefs = prefsManager
        if (singleUse && prefs != null) {
            if (hasFired()) return

            // If this sequence was started before, resume where the user left off.
            sequencePosition = prefs.getSequenceStatus()
            repeat(sequencePosition) { showcaseQueue.poll() }
        }

        showNextItem()
    }

    private fun showNextItem() {
        if (showcaseQueue.isEmpty()) {
            // End of the sequence.
            if (singleUse) prefsManager?.setFired()
            onFinishedListener?.onFinished(false)
            return
        }
        // Leaving mid-sequence (finishing, or being recreated for a configuration change): stop
        // here, but keep progress so we resume next time.
        if (activity.isFinishing || activity.isDestroyed || activity.isChangingConfigurations) return

        val sequenceItem = showcaseQueue.remove()
        sequenceItem.setDetachedListener(this)
        if (sequenceItem.show(activity)) {
            onItemShownListener?.onShow(sequenceItem, sequencePosition)
        } else {
            // The item can't be shown (e.g. it is single-use and has fired): move past it rather
            // than waiting forever for it to be dismissed.
            sequenceItem.setDetachedListener(null)
            advance()
        }
    }

    private fun advance() {
        sequencePosition++
        if (singleUse) prefsManager?.setSequenceStatus(sequencePosition)
        showNextItem()
    }

    override fun onShowcaseDetached(showcaseView: MaterialShowcaseView, wasDismissed: Boolean, wasSkipped: Boolean) {
        showcaseView.setDetachedListener(null)

        when {
            wasSkipped -> {
                onItemDismissedListener?.onDismiss(showcaseView, sequencePosition)
                onSkippedListener?.onSkip(showcaseView, sequencePosition)
                showcaseQueue.clear()
                if (singleUse) prefsManager?.setFired()
                onFinishedListener?.onFinished(true)
            }

            // Only advance if the item was deliberately dismissed, not just detached.
            wasDismissed -> {
                onItemDismissedListener?.onDismiss(showcaseView, sequencePosition)
                advance()
            }
        }
    }

    open fun setConfig(config: ShowcaseConfig?) {
        this.config = config
    }

    fun interface OnSequenceItemShownListener : SequenceItemCallbacks.Shown

    fun interface OnSequenceItemDismissedListener : SequenceItemCallbacks.Dismissed

    fun interface OnSequenceSkippedListener {
        fun onSkip(itemView: MaterialShowcaseView, position: Int)
    }

    fun interface OnSequenceFinishedListener {
        /** [skipped] is true if the user skipped the rest of the sequence. */
        fun onFinished(skipped: Boolean)
    }
}
