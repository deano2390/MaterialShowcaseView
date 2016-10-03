package uk.co.deanwild.materialshowcaseviewsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;

public class MaterialShowcaseWithCustomView extends MaterialShowcaseView {

    private OnClickListener targetOnClickListener;
    private int targetBoarderWidth;

    public MaterialShowcaseWithCustomView(Context context) {
        super(context);
    }

    public MaterialShowcaseWithCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MaterialShowcaseWithCustomView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public int getLayoutFile() {
        return uk.co.deanwild.materialshowcaseview.R.layout.showcase_with_custom_view_content;
    }

    public void setTargetViewOnclickListener(OnClickListener targetOnClickListener) {
        this.targetOnClickListener = targetOnClickListener;
    }

    public void setTargetBorderWidth(int width) {
        targetBoarderWidth = width;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (!mShouldRender) return;

        final int width = getMeasuredWidth();
        final int height = getMeasuredHeight();

        if(width <= 0 || height <= 0) return;

        if (mBitmap == null || mCanvas == null || mOldHeight != height || mOldWidth != width) {
            if (mBitmap != null) {
                mBitmap.recycle();
            }
            mBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            mCanvas = new Canvas(mBitmap);
        }

        mOldWidth = width;
        mOldHeight = height;

        mCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        mCanvas.drawColor(mMaskColour);

        if (mEraser == null) {
            mEraser = new Paint();
            mEraser.setFlags(Paint.ANTI_ALIAS_FLAG);
        }

        //Erase the background for the target area
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);

        //Draw the outer circle
        mEraser.setColor(ContextCompat.getColor(getContext(), R.color.babu));
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding);

        //Draw the inner cirle
        mEraser.setColor(ContextCompat.getColor(getContext(), R.color.transparent));
        mEraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        mShape.draw(mCanvas, mEraser, mXPosition, mYPosition, mShapePadding - targetBoarderWidth);

        canvas.drawBitmap(mBitmap, 0, 0, null);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(mTarget.getBounds().contains((int)event.getX(), (int)event.getY())){
            hide();
            targetOnClickListener.onClick(v);
            return false;
        }

        if (mDismissOnTouch) {
            hide();
        }
        return true;
    }

    public static class CustomBuilder extends Builder {

        public CustomBuilder(Activity activity) {
            super(activity);
            init(activity);
        }

        @Override
        protected void init(Activity activity) {
            showcaseView = new MaterialShowcaseWithCustomView(activity);
        }

        public CustomBuilder setTargetViewOnclickListener(OnClickListener targetOnClickListener) {
            ((MaterialShowcaseWithCustomView)showcaseView).setTargetViewOnclickListener(
                targetOnClickListener);
            return this;
        }

        public CustomBuilder setTargetBorderWidth(int width) {
            ((MaterialShowcaseWithCustomView)showcaseView).setTargetBorderWidth(width);
            return this;
        }
    }
}
