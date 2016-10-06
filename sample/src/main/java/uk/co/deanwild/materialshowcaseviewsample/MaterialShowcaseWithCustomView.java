package uk.co.deanwild.materialshowcaseviewsample;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView;
import uk.co.deanwild.materialshowcaseview.shape.Shape;

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
        return R.layout.showcase_with_custom_view_content;
    }

    public void setTargetViewOnclickListener(OnClickListener targetOnClickListener) {
        this.targetOnClickListener = targetOnClickListener;
    }

    public void setTargetBorderWidth(int width) {
        targetBoarderWidth = width;
    }


    @Override
    protected void drawBoarder(Canvas canvas, Paint eraser, Shape shape, int xPosition,
         int yPosition, int shapePadding) {
        //Draw the outer circle
        eraser.setColor(ContextCompat.getColor(getContext(), R.color.babu));
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.OVERLAY));
        shape.draw(canvas, eraser, xPosition, yPosition, shapePadding);

        //Draw the inner cirle
        eraser.setColor(ContextCompat.getColor(getContext(), R.color.transparent));
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        shape.draw(canvas, eraser, xPosition, yPosition, shapePadding - targetBoarderWidth);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(isTouchOnTargetView(event)){
            targetOnClickListener.onClick(v);
        }

        hide();
        return true;
    }

    protected int getContentBoxViewId() {
        return R.id.contentwrapper;
    }

    protected int getTitleViewId() {
        return R.id.title;
    }

    protected int getButtonViewId() {
        return R.id.dismiss;
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