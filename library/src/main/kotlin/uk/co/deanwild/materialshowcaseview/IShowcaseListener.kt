package uk.co.deanwild.materialshowcaseview

/** Observes a showcase being displayed and dismissed. */
interface IShowcaseListener {
    fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView)

    /** Called once when the showcase is removed, whether dismissed, skipped or detached. */
    fun onShowcaseDismissed(showcaseView: MaterialShowcaseView)
}
