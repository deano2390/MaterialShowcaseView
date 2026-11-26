package uk.co.deanwild.materialshowcaseview

import android.content.Context
import androidx.core.content.edit

/** Persists whether single-use showcases and sequences have been shown. */
open class PrefsManager(context: Context, private val showcaseID: String?) {

    // Hold the application context so we never leak an Activity.
    private val context: Context = context.applicationContext ?: context

    /* Individual showcases */

    internal fun hasFired(): Boolean = getSequenceStatus() == SEQUENCE_FINISHED

    internal fun setFired() = setSequenceStatus(SEQUENCE_FINISHED)

    /* Sequences */

    internal fun getSequenceStatus(): Int =
        prefs(context).getInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED)

    internal fun setSequenceStatus(status: Int) =
        prefs(context).edit { putInt(STATUS + showcaseID, status) }

    open fun resetShowcase() = resetShowcase(context, showcaseID)

    /** No longer required: the manager only holds the application context. */
    open fun close() = Unit

    companion object {
        const val SEQUENCE_NEVER_STARTED = 0
        const val SEQUENCE_FINISHED = -1

        private const val PREFS_NAME = "material_showcaseview_prefs"
        private const val STATUS = "status_"

        private fun prefs(context: Context) = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        @JvmStatic
        internal fun resetShowcase(context: Context, showcaseID: String?) =
            prefs(context).edit { putInt(STATUS + showcaseID, SEQUENCE_NEVER_STARTED) }

        @JvmStatic
        fun resetAll(context: Context) = prefs(context).edit { clear() }
    }
}
