package uk.co.deanwild.materialshowcaseview;

/**
 * Observes a showcase being displayed and dismissed.
 * <p>
 * Declared in Java (without nullability annotations) on purpose, so Kotlin implementations
 * written against 1.x keep compiling whether they declare parameters nullable or not.
 */
public interface IShowcaseListener {
    void onShowcaseDisplayed(MaterialShowcaseView showcaseView);

    /** Called once when the showcase is removed, whether dismissed, skipped or detached. */
    void onShowcaseDismissed(MaterialShowcaseView showcaseView);
}
