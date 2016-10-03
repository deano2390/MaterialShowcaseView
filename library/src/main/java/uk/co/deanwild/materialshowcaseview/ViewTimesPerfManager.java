package uk.co.deanwild.materialshowcaseview;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * The perf manager remembers whether user has seen the screen for the first time.
 */
public class ViewTimesPerfManager {
    private static final int NEVER_SEEN = 0;
    private static final int MULTIPLE_SEEN = 2;
    private static final int FIRST_SEEN = 1;
    private static final String NUX_SEEN = "seen_";
    private static final String PREFS_NAME = "onboarding_nux_first_seen_prefs";

    private String nuxID = null;
    private SharedPreferences sharedPreferences;

    public ViewTimesPerfManager(Context context, String nuxID) {
        this.nuxID = nuxID;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get whether the nux is being seen for the second time
     */
    public boolean hasSeenFirstTime() {
        return sharedPreferences.getInt(NUX_SEEN + nuxID, NEVER_SEEN) == FIRST_SEEN;
    }

    /**
     * Set the number of times the UI is being seen. Since the nux is only shown when the UI
     * is seen for the second time, after seen for the second time, there is no need to keep
     * increasing the number of how many times it is seen.
     */
    public void setHasSeen() {
        int getSeen = sharedPreferences.getInt(NUX_SEEN + nuxID, NEVER_SEEN);
        if (getSeen < MULTIPLE_SEEN) {
            sharedPreferences.edit().putInt(NUX_SEEN + nuxID, getSeen++).apply();
        }
    }
}
