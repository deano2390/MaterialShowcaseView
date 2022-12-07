package uk.co.deanwild.materialshowcaseview;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;


public class MaterialShowcaseSequence implements IDetachedListener {

    PrefsManager mPrefsManager;
    ArrayList<MaterialShowcaseView> mShowcaseQueue;
    MaterialShowcaseView currentView;
    private boolean mSingleUse = false;
    Activity mActivity;
    private ShowcaseConfig mConfig;
    private int mSequencePosition = 0;

    private OnSequenceItemShownListener mOnItemShownListener = null;
    private OnSequenceItemDismissedListener mOnItemDismissedListener = null;
    private OnSequenceSkippedListener mOnSkippedListener = null;
    private OnSequenceBackListener mOnBackListener = null;

    public MaterialShowcaseSequence(Activity activity) {
        mActivity = activity;
        mShowcaseQueue = new ArrayList<>();
    }

    public MaterialShowcaseSequence(Activity activity, String sequenceID) {
        this(activity);
        this.singleUse(sequenceID);
    }

    public MaterialShowcaseSequence addSequenceItem(View targetView, String content, String dismissText) {
        addSequenceItem(targetView, "", content, dismissText);
        return this;
    }
    private Boolean canFinishByGoingBack = false;
    public void setCanFinishByGoingBack(Boolean t){
        canFinishByGoingBack = t;
    }

    public MaterialShowcaseSequence addSequenceItem(View targetView, String title, String content, String dismissText) {

        MaterialShowcaseView sequenceItem = new MaterialShowcaseView.Builder(mActivity)
                .setTarget(targetView)
                .setTitleText(title)
                .setDismissText(dismissText)
                .setContentText(content)
                .setSequence(true)
                .build();

        if (mConfig != null) {
            sequenceItem.setConfig(mConfig);
        }

        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(MaterialShowcaseView sequenceItem) {

        if (mConfig != null) {
            sequenceItem.setConfig(mConfig);
        }

        mShowcaseQueue.add(sequenceItem);
        return this;
    }

    public MaterialShowcaseSequence singleUse(String sequenceID) {
        mSingleUse = true;
        mPrefsManager = new PrefsManager(mActivity, sequenceID);
        return this;
    }

    public void setOnItemShownListener(OnSequenceItemShownListener listener) {
        this.mOnItemShownListener = listener;
    }

    public void setOnItemDismissedListener(OnSequenceItemDismissedListener listener) {
        this.mOnItemDismissedListener = listener;
    }
    public void setOnSkippedListener(OnSequenceSkippedListener listener){
        this.mOnSkippedListener = listener;
    }
    public void setOnBackListener(OnSequenceBackListener listener){
        this.mOnBackListener = listener;
    }
    public boolean hasFired() {

        if (mPrefsManager.getSequenceStatus() == PrefsManager.SEQUENCE_FINISHED) {
            return true;
        }

        return false;
    }

    public boolean hasStarted() {

        if (mPrefsManager.getSequenceStatus() != PrefsManager.SEQUENCE_NEVER_STARTED) {
            return true;
        }
        return false;
    }

    public void start() {
        /**
         * Check if we've already shot our bolt and bail out if so         *
         */
        if (mSingleUse) {
            if (hasStarted()) {
                return;
            }


            /**
             * See if we have started this sequence before, if so then skip to the point we reached before
             * instead of showing the user everything from the start
             */
            mSequencePosition = mPrefsManager.getSequenceStatus();

//            if (mSequencePosition > 0) {
//                for (int i = 0; i < mSequencePosition; i++) {
//                    mShowcaseQueue.poll();
//
//                }
//            }
        }

        // do start
        //if (mShowcaseQueue.size() > mShowcaseQueue.)
        showNextItem();
    }

    public void setSequencePosition(int i){
        if(i > mShowcaseQueue.size()){
            mSequencePosition = mShowcaseQueue.size();
        }else{
            mSequencePosition = i;
        }
    }
    public void showLastItem() {
//        mShowcaseQueueCopy.

        if (mShowcaseQueue.size() > 0 && mSequencePosition >= 0  && !mActivity.isFinishing()) {
            MaterialShowcaseView sequenceItem = mShowcaseQueue.get(mSequencePosition);

//            mShowcaseQueue.add(sequenceItem);
            sequenceItem.setDetachedListener(this);
            sequenceItem.show(mActivity);
//            currentView = sequenceItem;
            if (mOnItemShownListener != null) {
                mOnItemShownListener.onShow(sequenceItem, mSequencePosition);
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if(mOnBackListener!=null){
                mOnBackListener.onBack(currentView, mSequencePosition);
            }
            if (mSingleUse) {
                mPrefsManager.setFired();
            }
        }
    }
    public void showNextItem() {

        if (mShowcaseQueue.size() > 0 && mSequencePosition < mShowcaseQueue.size() && !mActivity.isFinishing()) {
            MaterialShowcaseView sequenceItem = mShowcaseQueue.get(mSequencePosition);
            sequenceItem.setDetachedListener(this);
            sequenceItem.show(mActivity);
            currentView = sequenceItem;
            if (mOnItemShownListener != null) {
                mOnItemShownListener.onShow(sequenceItem, mSequencePosition);
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mPrefsManager.setFired();
            }
        }
    }


    public void skipTutorial() {

        mShowcaseQueue.clear();

        if (mShowcaseQueue.size() > 0 && !mActivity.isFinishing()) {
//            MaterialShowcaseView sequenceItem = mShowcaseQueue.remove();
            MaterialShowcaseView sequenceItem = mShowcaseQueue.get(mSequencePosition);

            sequenceItem.setDetachedListener(this);
            sequenceItem.show(mActivity);
            if (mOnItemShownListener != null) {
                mOnItemShownListener.onShow(sequenceItem, mSequencePosition);
            }
        } else {
            /**
             * We've reached the end of the sequence, save the fired state
             */
            if (mSingleUse) {
                mPrefsManager.setFired();
            }
        }
    }


    @Override
    public void onShowcaseDetached(MaterialShowcaseView showcaseView, boolean wasDismissed, boolean wasSkipped, boolean wasBack) {
        Log.d( "onShowcaseDetached: ", "bools:"+wasDismissed + wasSkipped + wasBack);
        Log.d( "onShowcaseDetached: ", "mSequencePosition:"+mSequencePosition);
        Log.d( "onShowcaseDetached: ", "size:"+mShowcaseQueue.size());

        showcaseView.setDetachedListener(null);

        /**
         * We're only interested if the showcase was purposefully dismissed
         */
        if (wasDismissed && !wasBack) {

            if (mOnItemDismissedListener != null) {
                mOnItemDismissedListener.onDismiss(showcaseView, mSequencePosition);
            }

            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
            if (mPrefsManager != null) {
                mSequencePosition++;
                mPrefsManager.setSequenceStatus(mSequencePosition);
            }

            showNextItem();
        }

        if(wasSkipped){
            if (mOnItemDismissedListener != null) {
                mOnItemDismissedListener.onDismiss(showcaseView, mSequencePosition);
            }
            if (mOnSkippedListener != null) {
                mOnSkippedListener.onSkip(showcaseView, mSequencePosition);

            }
            skipTutorial();


            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
//            if (mPrefsManager != null) {
//                mSequencePosition++;
//                mPrefsManager.setSequenceStatus(mSequencePosition);
//            }

        }
        if (wasBack) {
            /**
             * If so, update the prefsManager so we can potentially resume this sequence in the future
             */
            if (mPrefsManager != null) {
                mSequencePosition--;
                mPrefsManager.setSequenceStatus(mSequencePosition);
            }

            showLastItem();
        }

    }

    public void setConfig(ShowcaseConfig config) {
        this.mConfig = config;
    }

    public interface OnSequenceItemShownListener {
        void onShow(MaterialShowcaseView itemView, int position);
    }
    public interface OnSequenceSkippedListener {
        void onSkip(MaterialShowcaseView itemView, int position);
    }
    public interface OnSequenceBackListener {
        void onBack(MaterialShowcaseView itemView, int position);
    }
    public interface OnSequenceItemDismissedListener {
        void onDismiss(MaterialShowcaseView itemView, int position);
    }

}
