package uk.co.deanwild.materialshowcaseview.compat;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

/**
 * Interop detail: Java declarations of the {@code MaterialShowcaseSequence} listener methods, so
 * Kotlin implementations compile whether their parameters are declared nullable or not.
 * Implement {@code MaterialShowcaseSequence.OnSequenceItemShownListener} and friends instead.
 */
public final class SequenceItemCallbacks {
    private SequenceItemCallbacks() {
    }

    public interface Shown {
        void onShow(MaterialShowcaseView itemView, int position);
    }

    public interface Dismissed {
        void onDismiss(MaterialShowcaseView itemView, int position);
    }
}
