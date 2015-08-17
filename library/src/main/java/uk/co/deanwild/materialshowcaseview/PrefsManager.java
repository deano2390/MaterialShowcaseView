package uk.co.deanwild.materialshowcaseview;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by deanwild on 11/08/15.
 */
public class PrefsManager {
    private static final String PREFS_NAME = "material_showcaseview_prefs";
    private static final String FIRED_KEY = "has_fired_";
    private String showcaseID = null;
    private final Context context;

    public PrefsManager(Context context, String showcaseID) {
        this.context = context;
        this.showcaseID = showcaseID;
    }

    boolean hasFired() {
        return context
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(FIRED_KEY + showcaseID, false);
    }

    void setFired() {
        SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        internal.edit().putBoolean(FIRED_KEY + showcaseID, true).apply();

    }

    public void resetShowcase() {
        resetShowcase(context, showcaseID);
    }

    static void resetShowcase(Context context, String showcaseID){
        SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        internal.edit().putBoolean(FIRED_KEY + showcaseID, false).apply();
    }

    public static void resetAll(Context context){
        SharedPreferences internal = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        internal.edit().clear().apply();
    }
}
