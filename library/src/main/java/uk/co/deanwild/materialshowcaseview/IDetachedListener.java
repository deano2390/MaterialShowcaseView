package uk.co.deanwild.materialshowcaseview;

/** Hook used by {@link MaterialShowcaseSequence} to learn how each item ended. */
public interface IDetachedListener {
    void onShowcaseDetached(MaterialShowcaseView showcaseView, boolean wasDismissed, boolean wasSkipped);
}
