package uk.co.deanwild.materialshowcaseview;

import android.graphics.Color;

/**
 * Created by deanwild on 11/08/15.
 */
public class ShowcaseConfig {

    public static final String DEFAULT_MASK_COLOUR = "#dd335075";
    public static final long DEFAULT_FADE_TIME = 300;
    public static final long DEFAULT_DELAY = 0;

    private long mDelay = DEFAULT_DELAY;
    private int mMaskColour;
    private int mContentTextColor;
    private int mDismissTextColor;
    private long mFadeDuration = DEFAULT_FADE_TIME;

    public ShowcaseConfig() {
        mMaskColour = Color.parseColor(ShowcaseConfig.DEFAULT_MASK_COLOUR);
        mContentTextColor = Color.parseColor("#ffffff");
        mDismissTextColor = Color.parseColor("#ffffff");
    }

    public long getDelay() {
        return mDelay;
    }

    public void setDelay(long delay) {
        this.mDelay = delay;
    }

    public int getMaskColor() {
        return mMaskColour;
    }

    public void setMaskColor(int maskColor) {
        mMaskColour = maskColor;
    }

    public int getContentTextColor() {
        return mContentTextColor;
    }

    public void setContentTextColor(int mContentTextColor) {
        this.mContentTextColor = mContentTextColor;
    }

    public int getDismissTextColor() {
        return mDismissTextColor;
    }

    public void setDismissTextColor(int dismissTextColor) {
        this.mDismissTextColor = dismissTextColor;
    }

    public long getFadeDuration() {
        return mFadeDuration;
    }

    public void setFadeDuration(long fadeDuration) {
        this.mFadeDuration = fadeDuration;
    }
}
