package uk.co.deanwild.materialshowcaseview.adapter;

import android.app.Activity;
import android.os.Handler;
import android.support.annotation.ArrayRes;
import android.support.annotation.IdRes;
import android.support.annotation.StringRes;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseSequence;
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.ShowcaseConfig;

/**
 * Created by TurboCoder (Yamil García Hernández) on 7/7/16.
 */
public abstract class MaterialShowCaseAdapter implements MaterialShowcaseSequence.OnSequenceItemDismissedListener {

    // Constants
    protected String SHOW_CASE_ID;
    public final static int SHOW_CASE_DELAY = 5;

    public static final ArrayList<MaterialShowcaseSequence> POOL = new ArrayList<>();

    // Variables
    protected Activity activity;
    private MaterialShowcaseSequence sequence;
    protected ShowcaseConfig config;
    private ArrayList<MaterialShowcaseView> queue = new ArrayList<>();
    protected int currentQueueItemId = 0;

    public MaterialShowCaseAdapter(Activity activity, int delay, String id) {
        this.activity = activity;
        this.config = new ShowcaseConfig();
        this.SHOW_CASE_ID = id;
        config.setDelay(SHOW_CASE_DELAY);
        sequence = new MaterialShowcaseSequence(activity, id);
        sequence.setConfig(config);
    }

    public abstract void setup();

    public void start() {
        setup();
        MaterialShowCaseAdapter.POOL.add(sequence);
        sequence.setOnItemDismissedListener(this);
        if (POOL.size() <= 1)
            sequence.start();
    }

    public static void initialize(Activity activity, Class clazz) {
        try {
            Constructor<?> constructor = clazz.getConstructor(Activity.class);
            Object obj = constructor.newInstance(new Object[]{activity});

            if (obj instanceof MaterialShowCaseAdapter)
                ((MaterialShowCaseAdapter) obj).start();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public static void initializeWithDelay(final Activity activity, final Class clazz, long delay) {
        try {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        Constructor<?> constructor = clazz.getConstructor(Activity.class);
                        Object obj = constructor.newInstance(new Object[]{activity});

                        if (obj instanceof MaterialShowCaseAdapter)
                            ((MaterialShowCaseAdapter) obj).start();
                    } catch (NoSuchMethodException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                }
            }, delay);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void initialize(Activity activity, @ArrayRes int config, String id) {
        try {
            (new SimpleMaterialShowCaseAdapter(activity, activity.getResources().obtainTypedArray(config), id)).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void initializeWithDelay(final Activity activity, @ArrayRes final int config, final String id, long delay) {
        try {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    (new SimpleMaterialShowCaseAdapter(activity, activity.getResources().obtainTypedArray(config), id)).start();
                }
            }, delay);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addToQueue(MaterialShowcaseView v) {
        queue.add(v);
        sequence.addSequenceItem(v);
    }

    public void addToQueue(@IdRes int i, String title, String content, String button, MaterialShowCaseViewShape shape) {
        MaterialShowcaseView.Builder msvb = new MaterialShowcaseView.Builder(activity)
                .setTarget(activity.findViewById(i))
                .setTitleText(title)
                .setContentText(content)
                .setDismissText(button);

        switch (shape) {
            case CIRCLE:
                msvb.withCircleShape();
                break;
            case RECTANGLE:
                msvb.withRectangleShape();
                break;
        }

        MaterialShowcaseView msv = msvb.build();

        queue.add(msv);
        sequence.addSequenceItem(msv);
    }

    public void addToQueue(@IdRes int i, @StringRes int title, @StringRes int content, @StringRes int button, MaterialShowCaseViewShape shape) {
        MaterialShowcaseView.Builder msvb = new MaterialShowcaseView.Builder(activity)
                .setTarget(activity.findViewById(i))
                .setTitleText(title)
                .setContentText(content)
                .setDismissText(button);

        switch (shape) {
            case CIRCLE:
                msvb.withCircleShape();
                break;
            case RECTANGLE:
                msvb.withRectangleShape();
                break;
        }

        MaterialShowcaseView msv = msvb.build();

        queue.add(msv);
        sequence.addSequenceItem(msv);
    }


    @Override
    public void onDismiss(MaterialShowcaseView materialShowcaseView, int i) {
        if (currentQueueItemId == (queue.size() - 1)) {
            MaterialShowCaseAdapter.POOL.remove(0);
            if (POOL.size() > 0)
                MaterialShowCaseAdapter.POOL.get(0).start();
        }
        currentQueueItemId++;
    }

    public enum MaterialShowCaseViewShape {
        CIRCLE, RECTANGLE
    }
}
