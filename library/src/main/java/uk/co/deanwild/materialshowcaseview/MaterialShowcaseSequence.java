package uk.co.deanwild.materialshowcaseview;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by deanwild on 11/08/15.
 */
public class MaterialShowcaseSequence implements IShowcaseListener {

    PrefsManager mPrefsManager;
    Queue<MaterialShowcaseView> mShowcaseQueue;
    private boolean mSingleUse = false;
    Activity mActivity;
    private ShowcaseConfig mConfig;

    public MaterialShowcaseSequence(Activity activity) {
        mActivity = activity;
        mShowcaseQueue = new LinkedList<>();
    }

    public MaterialShowcaseSequence(Activity activity, String sequenceID) {
        this(activity);
        this.singleUse(sequenceID);
    }

    public MaterialShowcaseSequence addSequenceItem(View targetView, String content, String dismissText) {

        MaterialShowcaseView sequenceItem = new MaterialShowcaseView.Builder(mActivity)
                .setTarget(targetView)
                .setDismissText(dismissText)
                .setContentText(content)
                .build();

        if(mConfig!=null){
            sequenceItem.setConfig(mConfig);
        }

        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(MaterialShowcaseView sequenceItem) {
        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public MaterialShowcaseSequence singleUse(String sequenceID) {
        mSingleUse = true;
        mPrefsManager = new PrefsManager(mActivity, sequenceID);
        return this;
    }

    public void start() {

        /**
         * Check if we'e already shot our bolt and bail out if so
         * Otherwise, notify the prefsManager that we're firing now
         */
        if (mSingleUse) {
            if (mPrefsManager.hasFired()) {
                return;
            }
        }

        // do start
        if (mShowcaseQueue.size() > 0)
            showNextItem();
    }

    private void showNextItem() {

        if (mShowcaseQueue.size() > 0 && !mActivity.isFinishing()){
            MaterialShowcaseView sequenceItem = mShowcaseQueue.remove();
            sequenceItem.addShowcaseListener(this);
            sequenceItem.show(mActivity);
        }else{
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mPrefsManager.setFired();
            }
        }
    }

    @Override
    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {
        // don't care
    }

    @Override
    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
        showcaseView.removeShowcaseListener(this);
        showNextItem();
    }

    public void setConfig(ShowcaseConfig config) {
        this.mConfig = config;
    }
}
