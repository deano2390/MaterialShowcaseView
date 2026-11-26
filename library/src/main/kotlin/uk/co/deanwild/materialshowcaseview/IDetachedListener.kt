package uk.co.deanwild.materialshowcaseview

/** Internal hook used by [MaterialShowcaseSequence] to learn how each item ended. */
fun interface IDetachedListener {
    fun onShowcaseDetached(showcaseView: MaterialShowcaseView, wasDismissed: Boolean, wasSkipped: Boolean)
}
