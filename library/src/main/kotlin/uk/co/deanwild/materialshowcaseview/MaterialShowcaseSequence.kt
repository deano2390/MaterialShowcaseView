package uk.co.deanwild.materialshowcaseview

import android.app.Activity
import android.view.View
import java.util.LinkedList
import java.util.Queue

/**
 * Shows a series of [MaterialShowcaseView]s one after another.
 *
 * With a sequence ID (single-use) the sequence remembers how far the user got, resumes
 * from there next time, and never shows again once finished or skipped.
 */
open class MaterialShowcaseSequence(private val activity: Activity) : IDetachedListener {

    internal var prefsManager: PrefsManager? = null
        private set
    private val showcaseQueue: Queue<MaterialShowcaseView> = LinkedList()
    private var singleUse = false
    private var config: ShowcaseConfig? = null
    private var sequencePosition = 0

    private var onItemShownListener: OnSequenceItemShownListener? = null
    private var onItemDismissedListener: OnSequenceItemDismissedListener? = null

    constructor(activity: Activity, sequenceID: String) : this(activity) {
        singleUse(sequenceID)
    }

    open fun addSequenceItem(targetView: View, content: String?, dismissText: String?): MaterialShowcaseSequence =
        addSequenceItem(targetView, "", content, dismissText)

    open fun addSequenceItem(
        targetView: View,
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
    open fun addSequenceItem(sequenceItem: MaterialShowcaseView): MaterialShowcaseSequence {
        config?.let { sequenceItem.setConfig(it) }
        showcaseQueue.add(sequenceItem)
        return this
    }

    open fun singleUse(sequenceID: String): MaterialShowcaseSequence {
        singleUse = true
        prefsManager = PrefsManager(activity, sequenceID)
        return this
    }

    open fun setOnItemShownListener(listener: OnSequenceItemShownListener?) {
        onItemShownListener = listener
    }

    open fun setOnItemDismissedListener(listener: OnSequenceItemDismissedListener?) {
        onItemDismissedListener = listener
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
            return
        }
        // Leaving mid-sequence: stop here, but keep progress so we resume next time.
        if (activity.isFinishing) return

        val sequenceItem = showcaseQueue.remove()
        sequenceItem.setDetachedListener(this)
        sequenceItem.show(activity)
        onItemShownListener?.onShow(sequenceItem, sequencePosition)
    }

    override fun onShowcaseDetached(showcaseView: MaterialShowcaseView, wasDismissed: Boolean, wasSkipped: Boolean) {
        showcaseView.setDetachedListener(null)

        when {
            wasSkipped -> {
                onItemDismissedListener?.onDismiss(showcaseView, sequencePosition)
                showcaseQueue.clear()
                if (singleUse) prefsManager?.setFired()
            }

            // Only advance if the item was deliberately dismissed, not just detached.
            wasDismissed -> {
                onItemDismissedListener?.onDismiss(showcaseView, sequencePosition)
                sequencePosition++
                if (singleUse) prefsManager?.setSequenceStatus(sequencePosition)
                showNextItem()
            }
        }
    }

    open fun setConfig(config: ShowcaseConfig?) {
        this.config = config
    }

    fun interface OnSequenceItemShownListener {
        fun onShow(itemView: MaterialShowcaseView, position: Int)
    }

    fun interface OnSequenceItemDismissedListener {
        fun onDismiss(itemView: MaterialShowcaseView, position: Int)
    }
}
