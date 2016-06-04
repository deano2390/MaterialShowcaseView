package uk.co.deanwild.materialshowcaseview;

import android.app.Activity;
import android.view.View;

import java.util.LinkedList;
import java.util.Queue;


public class MaterialShowcaseSequence implements IDetachedListener {

    PrefsManager mPrefsManager;
    Queue<MaterialShowcaseView> mShowcaseQueue;
    private boolean mSingleUse = false;
    Activity mActivity;
    private ShowcaseConfig mConfig;
    private int mSequencePosition = 0;

    private OnSequenceItemShownListener mOnItemShownListener = null;
    private OnSequenceItemDismissedListener mOnItemDismissedListener = null;

    private MaterialShowcaseView mCurrentShownShowcase;

    public MaterialShowcaseSequence(Activity activity) {
        mActivity = activity;
        mShowcaseQueue = new LinkedList<>();
    }

    public MaterialShowcaseSequence(Activity activity, String sequenceID) {
        this(activity);
        this.singleUse(sequenceID);
    }

    public MaterialShowcaseSequence addSequenceItem(int contentResId, int dismissTextResId) {
        addSequenceItem(mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(String content, String dismissText) {
        addSequenceItem(null, content, dismissText);
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(int targetViewId, int contentResId, int dismissTextResId) {
        addSequenceItem(mActivity.findViewById(targetViewId), mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(int targetViewId, int titleResId, int contentResId, int dismissTextResId) {
        addSequenceItem(mActivity.findViewById(targetViewId), mActivity.getString(titleResId),
                mActivity.getString(contentResId), mActivity.getString(dismissTextResId));
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(View targetView, String content, String dismissText) {
        addSequenceItem(targetView, "", content, dismissText);
        return this;
    }

    public MaterialShowcaseSequence addSequenceItem(View targetView, String title, String content, String dismissText) {

        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(mActivity)
                .setTitleText(title)
                .setDismissText(dismissText)
                .setContentText(content);

        if (targetView != null)
            builder.setTarget(targetView);
        else
            builder.withoutShape();

        MaterialShowcaseView sequenceItem = builder.build();

        if (mConfig != null) {
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

    public void setOnItemShownListener(OnSequenceItemShownListener listener) {
        this.mOnItemShownListener = listener;
    }

    public void setOnItemDismissedListener(OnSequenceItemDismissedListener listener) {
        this.mOnItemDismissedListener = listener;
    }

    public boolean hasFired() {
        return mPrefsManager.getSequenceStatus() == PrefsManager.SEQUENCE_FINISHED;
    }

    public void start() {

        /**
         * Check if we've already shot our bolt and bail out if so         *
         */
        if (mSingleUse) {
            if (hasFired()) {
                return;
            }

            /**
             * See if we have started this sequence before, if so then skip to the point we reached before
             * instead of showing the user everything from the start
             */
            mSequencePosition = mPrefsManager.getSequenceStatus();

            if (mSequencePosition > 0) {
                for (int i = 0; i < mSequencePosition; i++) {
                    mShowcaseQueue.poll();
                }
            }
        }


        // do start
        if (mShowcaseQueue.size() > 0)
            showNextItem();
    }

    private void showNextItem() {

        if (mShowcaseQueue.size() > 0 && !mActivity.isFinishing()) {
            mCurrentShownShowcase = mShowcaseQueue.remove();
            mCurrentShownShowcase.setDetachedListener(this);
            mCurrentShownShowcase.show(mActivity);
            if (mOnItemShownListener != null) {
                mOnItemShownListener.onShow(mCurrentShownShowcase, mSequencePosition);
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

    // Note: This is not meant to be a pause method, it will act like if user has finished this sequence
    public void cancel() {

        /**
         * Act like we've reached the end of the sequence
         */
        while (mShowcaseQueue.poll() != null)
            mSequencePosition++;

        if (mPrefsManager != null)
            mPrefsManager.setSequenceStatus(mSequencePosition);

        if (mCurrentShownShowcase != null)
            mCurrentShownShowcase.hide();
    }


    @Override
    public void onShowcaseDetached(MaterialShowcaseView showcaseView, boolean wasDismissed) {

        mCurrentShownShowcase = null;
        showcaseView.setDetachedListener(null);

        /**
         * We're only interested if the showcase was purposefully dismissed
         */
        if (wasDismissed) {

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
    }

    public void setConfig(ShowcaseConfig config) {
        this.mConfig = config;
    }

    public interface OnSequenceItemShownListener {
        void onShow(MaterialShowcaseView itemView, int position);
    }

    public interface OnSequenceItemDismissedListener {
        void onDismiss(MaterialShowcaseView itemView, int position);
    }

}
