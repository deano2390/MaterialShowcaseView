package uk.co.deanwild.materialshowcaseview;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;


import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.Arrays;

/**
 * Created by florentchampigny on 02/06/2017.
 */

public class ShowcaseTooltip {

    private View rootView;
    private View view;
    private TooltipView tooltip_view;


    private ShowcaseTooltip(Context context){
        MyContext myContext = new MyContext(getActivityContext(context));
        this.tooltip_view = new TooltipView(myContext.getContext());
    }

    public static ShowcaseTooltip build(Context context) {
        return new ShowcaseTooltip(context);
    }

    public void configureTarget(ViewGroup rootView, View view) {
        this.rootView = rootView;
        this.view = view;
    }

    private static Activity getActivityContext(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) {
                return (Activity) context;
            }
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    public ShowcaseTooltip position(Position position) {
        this.tooltip_view.setPosition(position);
        return this;
    }

    public ShowcaseTooltip customView(View customView) {
        this.tooltip_view.setCustomView(customView);
        return this;
    }

    public ShowcaseTooltip customView(int viewId) {
        this.tooltip_view.setCustomView(((Activity) view.getContext()).findViewById(viewId));
        return this;
    }

    public ShowcaseTooltip arrowWidth(int arrowWidth) {
        this.tooltip_view.setArrowWidth(arrowWidth);
        return this;
    }

    public ShowcaseTooltip arrowHeight(int arrowHeight) {
        this.tooltip_view.setArrowHeight(arrowHeight);
        return this;
    }

    public ShowcaseTooltip arrowSourceMargin(int arrowSourceMargin) {
        this.tooltip_view.setArrowSourceMargin(arrowSourceMargin);
        return this;
    }

    public ShowcaseTooltip arrowTargetMargin(int arrowTargetMargin) {
        this.tooltip_view.setArrowTargetMargin(arrowTargetMargin);
        return this;
    }

    public ShowcaseTooltip align(ALIGN align) {
        this.tooltip_view.setAlign(align);
        return this;
    }

    public TooltipView show(final int margin) {
        final Context activityContext = tooltip_view.getContext();
        if (activityContext != null && activityContext instanceof Activity) {
            final ViewGroup decorView = rootView != null ?
                    (ViewGroup) rootView :
                    (ViewGroup) ((Activity) activityContext).getWindow().getDecorView();

            view.postDelayed(new Runnable() {
                @Override
                public void run() {
                    final Rect rect = new Rect();
                    view.getGlobalVisibleRect(rect);

                    final Rect rootGlobalRect = new Rect();
                    final Point rootGlobalOffset = new Point();
                    decorView.getGlobalVisibleRect(rootGlobalRect, rootGlobalOffset);

                    int[] location = new int[2];
                    view.getLocationOnScreen(location);
                    rect.left = location[0];
                    if (rootGlobalOffset != null) {
                        rect.top -= rootGlobalOffset.y;
                        rect.bottom -= rootGlobalOffset.y;
                        rect.left -= rootGlobalOffset.x;
                        rect.right -= rootGlobalOffset.x;
                    }

                    // fixes bottom mode
                    rect.top -= margin;

                    // fixes top mode
                    rect.bottom += margin;

                    decorView.addView(tooltip_view, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                    tooltip_view.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {

                            tooltip_view.setup(rect, decorView.getWidth());

                            tooltip_view.getViewTreeObserver().removeOnPreDrawListener(this);

                            return false;
                        }
                    });
                }
            }, 100);
        }
        return tooltip_view;
    }

    public ShowcaseTooltip color(int color) {
        this.tooltip_view.setColor(color);
        return this;
    }

    public ShowcaseTooltip color(Paint paint) {
        this.tooltip_view.setPaint(paint);
        return this;
    }

    public ShowcaseTooltip onDisplay(ListenerDisplay listener) {
        this.tooltip_view.setListenerDisplay(listener);
        return this;
    }

    public ShowcaseTooltip padding(int left, int top, int right, int bottom) {
        this.tooltip_view.paddingTop = top;
        this.tooltip_view.paddingBottom = bottom;
        this.tooltip_view.paddingLeft = left;
        this.tooltip_view.paddingRight = right;
        return this;
    }

    public ShowcaseTooltip animation(TooltipAnimation tooltipAnimation) {
        this.tooltip_view.setTooltipAnimation(tooltipAnimation);
        return this;
    }

    public ShowcaseTooltip text(String text) {
        this.tooltip_view.setText(text);
        return this;
    }

    public ShowcaseTooltip text(int text) {
        this.tooltip_view.setText(text);
        return this;
    }

    public ShowcaseTooltip corner(int corner) {
        this.tooltip_view.setCorner(corner);
        return this;
    }

    public ShowcaseTooltip textColor(int textColor) {
        this.tooltip_view.setTextColor(textColor);
        return this;
    }

    public ShowcaseTooltip textTypeFace(Typeface typeface) {
        this.tooltip_view.setTextTypeFace(typeface);
        return this;
    }

    public ShowcaseTooltip textSize(int unit, float textSize) {
        this.tooltip_view.setTextSize(unit, textSize);
        return this;
    }

    public ShowcaseTooltip setTextGravity(int textGravity) {
        this.tooltip_view.setTextGravity(textGravity);
        return this;
    }

    public ShowcaseTooltip distanceWithView(int distance) {
        this.tooltip_view.setDistanceWithView(distance);
        return this;
    }

    public ShowcaseTooltip border(int color, float width) {
        Paint borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(color);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(width);
        this.tooltip_view.setBorderPaint(borderPaint);
        return this;
    }

    public enum Position {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
    }

    public enum ALIGN {
        START,
        CENTER,
        END
    }

    public interface TooltipAnimation {
        void animateEnter(View view, Animator.AnimatorListener animatorListener);

        void animateExit(View view, Animator.AnimatorListener animatorListener);
    }

    public interface ListenerDisplay {
        void onDisplay(View view);
    }

    public static class FadeTooltipAnimation implements TooltipAnimation {

        private long fadeDuration = 400;

        public FadeTooltipAnimation() {
        }

        public FadeTooltipAnimation(long fadeDuration) {
            this.fadeDuration = fadeDuration;
        }

        @Override
        public void animateEnter(View view, Animator.AnimatorListener animatorListener) {
            view.setAlpha(0);
            view.animate().alpha(1).setDuration(fadeDuration).setListener(animatorListener);
        }

        @Override
        public void animateExit(View view, Animator.AnimatorListener animatorListener) {
            view.animate().alpha(0).setDuration(fadeDuration).setListener(animatorListener);
        }
    }

    public static class TooltipView extends FrameLayout {

        private static final int MARGIN_SCREEN_BORDER_TOOLTIP = 30;
        private int arrowHeight = 15;
        private int arrowWidth = 15;
        private int arrowSourceMargin = 0;
        private int arrowTargetMargin = 0;
        protected View childView;
        private int color = Color.parseColor("#FFFFFF");
        private Path bubblePath;
        private Paint bubblePaint;
        private Paint borderPaint;
        private Position position = Position.BOTTOM;
        private ALIGN align = ALIGN.CENTER;

        private ListenerDisplay listenerDisplay;

        private TooltipAnimation tooltipAnimation = new FadeTooltipAnimation();

        private int corner = 30;

        private int paddingTop = 20;
        private int paddingBottom = 30;
        private int paddingRight = 30;
        private int paddingLeft = 30;

        private Rect viewRect;
        private int distanceWithView = 0;

        public TooltipView(Context context) {
            super(context);
            setWillNotDraw(false);

            this.childView = new TextView(context);
            ((TextView) childView).setTextColor(Color.BLACK);
            addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            childView.setPadding(0, 0, 0, 0);

            bubblePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
            bubblePaint.setColor(color);
            bubblePaint.setStyle(Paint.Style.FILL);

            borderPaint = null;

            setLayerType(LAYER_TYPE_SOFTWARE, bubblePaint);

        }

        public void setCustomView(View customView) {
            this.removeView(childView);
            this.childView = customView;
            addView(childView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        public void setColor(int color) {
            this.color = color;
            bubblePaint.setColor(color);
            postInvalidate();
        }

        public void setPaint(Paint paint) {
            bubblePaint = paint;
            setLayerType(LAYER_TYPE_SOFTWARE, paint);
            postInvalidate();
        }

        public void setPosition(Position position) {
            this.position = position;
            switch (position) {
                case TOP:
                    setPadding(paddingLeft, paddingTop, paddingRight, paddingBottom + arrowHeight);
                    break;
                case BOTTOM:
                    setPadding(paddingLeft, paddingTop + arrowHeight, paddingRight, paddingBottom);
                    break;
                case LEFT:
                    setPadding(paddingLeft, paddingTop, paddingRight + arrowHeight, paddingBottom);
                    break;
                case RIGHT:
                    setPadding(paddingLeft + arrowHeight, paddingTop, paddingRight, paddingBottom);
                    break;
            }
            postInvalidate();
        }

        public void setAlign(ALIGN align) {
            this.align = align;
            postInvalidate();
        }

        public void setText(String text) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setText(Html.fromHtml(text));
            }
            postInvalidate();
        }

        public void setText(int text) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setText(text);
            }
            postInvalidate();
        }

        public void setTextColor(int textColor) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setTextColor(textColor);
            }
            postInvalidate();
        }

        public int getArrowHeight() {
            return arrowHeight;
        }

        public void setArrowHeight(int arrowHeight) {
            this.arrowHeight = arrowHeight;
            postInvalidate();
        }

        public int getArrowWidth() {
            return arrowWidth;
        }

        public void setArrowWidth(int arrowWidth) {
            this.arrowWidth = arrowWidth;
            postInvalidate();
        }

        public int getArrowSourceMargin() {
            return arrowSourceMargin;
        }

        public void setArrowSourceMargin(int arrowSourceMargin) {
            this.arrowSourceMargin = arrowSourceMargin;
            postInvalidate();
        }

        public int getArrowTargetMargin() {
            return arrowTargetMargin;
        }

        public void setArrowTargetMargin(int arrowTargetMargin) {
            this.arrowTargetMargin = arrowTargetMargin;
            postInvalidate();
        }

        public void setTextTypeFace(Typeface textTypeFace) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setTypeface(textTypeFace);
            }
            postInvalidate();
        }

        public void setTextSize(int unit, float size) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setTextSize(unit, size);
            }
            postInvalidate();
        }

        public void setTextGravity(int textGravity) {
            if (childView instanceof TextView) {
                ((TextView) this.childView).setGravity(textGravity);
            }
            postInvalidate();
        }

        public void setCorner(int corner) {
            this.corner = corner;
        }

        @Override
        protected void onSizeChanged(int width, int height, int oldw, int oldh) {
            super.onSizeChanged(width, height, oldw, oldh);

            bubblePath = drawBubble(new RectF(0, 0, width, height), corner, corner, corner, corner);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);

            if (bubblePath != null) {
                canvas.drawPath(bubblePath, bubblePaint);
                if (borderPaint != null) {
                    canvas.drawPath(bubblePath, borderPaint);
                }
            }
        }

        public void setListenerDisplay(ListenerDisplay listener) {
            this.listenerDisplay = listener;
        }

        public void setTooltipAnimation(TooltipAnimation tooltipAnimation) {
            this.tooltipAnimation = tooltipAnimation;
        }

        protected void startEnterAnimation() {
            tooltipAnimation.animateEnter(this, new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (listenerDisplay != null) {
                        listenerDisplay.onDisplay(TooltipView.this);
                    }
                }
            });
        }

        public void setupPosition(Rect rect) {

            int x, y;

            if (position == Position.LEFT || position == Position.RIGHT) {
                if (position == Position.LEFT) {
                    x = rect.left - getWidth() - distanceWithView;
                } else {
                    x = rect.right + distanceWithView;
                }
                y = rect.top + getAlignOffset(getHeight(), rect.height());
            } else {
                if (position == Position.BOTTOM) {
                    y = rect.bottom + distanceWithView;
                } else { // top
                    y = rect.top - getHeight() - distanceWithView;
                }
                x = rect.left + getAlignOffset(getWidth(), rect.width());
            }

            setTranslationX(x);
            setTranslationY(y);
        }

        private int getAlignOffset(int myLength, int hisLength) {
            switch (align) {
                case END:
                    return hisLength - myLength;
                case CENTER:
                    return (hisLength - myLength) / 2;
            }
            return 0;
        }

        private Path drawBubble(RectF myRect, float topLeftDiameter, float topRightDiameter, float bottomRightDiameter, float bottomLeftDiameter) {
            final Path path = new Path();

            if (viewRect == null)
                return path;

            topLeftDiameter = topLeftDiameter < 0 ? 0 : topLeftDiameter;
            topRightDiameter = topRightDiameter < 0 ? 0 : topRightDiameter;
            bottomLeftDiameter = bottomLeftDiameter < 0 ? 0 : bottomLeftDiameter;
            bottomRightDiameter = bottomRightDiameter < 0 ? 0 : bottomRightDiameter;

            final float spacingLeft = this.position == Position.RIGHT ? arrowHeight : 0;
            final float spacingTop = this.position == Position.BOTTOM ? arrowHeight : 0;
            final float spacingRight = this.position == Position.LEFT ? arrowHeight : 0;
            final float spacingBottom = this.position == Position.TOP ? arrowHeight : 0;

            final float left = spacingLeft + myRect.left;
            final float top = spacingTop + myRect.top;
            final float right = myRect.right - spacingRight;
            final float bottom = myRect.bottom - spacingBottom;
            final float centerX = viewRect.centerX() - getX();

            final float arrowSourceX = (Arrays.asList(Position.TOP, Position.BOTTOM).contains(this.position))
                    ? centerX + arrowSourceMargin
                    : centerX;
            final float arrowTargetX = (Arrays.asList(Position.TOP, Position.BOTTOM).contains(this.position))
                    ? centerX + arrowTargetMargin
                    : centerX;
            final float arrowSourceY = (Arrays.asList(Position.RIGHT, Position.LEFT).contains(this.position))
                    ? bottom / 2f - arrowSourceMargin
                    : bottom / 2f;
            final float arrowTargetY = (Arrays.asList(Position.RIGHT, Position.LEFT).contains(this.position))
                    ? bottom / 2f - arrowTargetMargin
                    : bottom / 2f;

            path.moveTo(left + topLeftDiameter / 2f, top);
            //LEFT, TOP

            if (position == Position.BOTTOM) {
                path.lineTo(arrowSourceX - arrowWidth, top);
                path.lineTo(arrowTargetX, myRect.top);
                path.lineTo(arrowSourceX + arrowWidth, top);
            }
            path.lineTo(right - topRightDiameter / 2f, top);

            path.quadTo(right, top, right, top + topRightDiameter / 2);
            //RIGHT, TOP

            if (position == Position.LEFT) {
                path.lineTo(right, arrowSourceY - arrowWidth);
                path.lineTo(myRect.right, arrowTargetY);
                path.lineTo(right, arrowSourceY + arrowWidth);
            }
            path.lineTo(right, bottom - bottomRightDiameter / 2);

            path.quadTo(right, bottom, right - bottomRightDiameter / 2, bottom);
            //RIGHT, BOTTOM

            if (position == Position.TOP) {
                path.lineTo(arrowSourceX + arrowWidth, bottom);
                path.lineTo(arrowTargetX, myRect.bottom);
                path.lineTo(arrowSourceX - arrowWidth, bottom);
            }
            path.lineTo(left + bottomLeftDiameter / 2, bottom);

            path.quadTo(left, bottom, left, bottom - bottomLeftDiameter / 2);
            //LEFT, BOTTOM

            if (position == Position.RIGHT) {
                path.lineTo(left, arrowSourceY + arrowWidth);
                path.lineTo(myRect.left, arrowTargetY);
                path.lineTo(left, arrowSourceY - arrowWidth);
            }
            path.lineTo(left, top + topLeftDiameter / 2);

            path.quadTo(left, top, left + topLeftDiameter / 2, top);

            path.close();

            return path;
        }

        public boolean adjustSize(Rect rect, int screenWidth) {

            final Rect r = new Rect();
            getGlobalVisibleRect(r);

            boolean changed = false;
            final ViewGroup.LayoutParams layoutParams = getLayoutParams();
            if (position == Position.LEFT && getWidth() > rect.left) {
                layoutParams.width = rect.left - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView;
                changed = true;
            } else if (position == Position.RIGHT && rect.right + getWidth() > screenWidth) {
                layoutParams.width = screenWidth - rect.right - MARGIN_SCREEN_BORDER_TOOLTIP - distanceWithView;
                changed = true;
            } else if (position == Position.TOP || position == Position.BOTTOM) {
                int adjustedLeft = rect.left;
                int adjustedRight = rect.right;

                if ((rect.centerX() + getWidth() / 2f) > screenWidth) {
                    float diff = (rect.centerX() + getWidth() / 2f) - screenWidth;

                    adjustedLeft -= diff;
                    adjustedRight -= diff;

                    setAlign(ALIGN.CENTER);
                    changed = true;
                } else if ((rect.centerX() - getWidth() / 2f) < 0) {
                    float diff = -(rect.centerX() - getWidth() / 2f);

                    adjustedLeft += diff;
                    adjustedRight += diff;

                    setAlign(ALIGN.CENTER);
                    changed = true;
                }

                if (adjustedLeft < 0) {
                    adjustedLeft = 0;
                }

                if (adjustedRight > screenWidth) {
                    adjustedRight = screenWidth;
                }

                rect.left = adjustedLeft;
                rect.right = adjustedRight;
            }

            setLayoutParams(layoutParams);
            postInvalidate();
            return changed;
        }

        private void onSetup(Rect myRect) {
            setupPosition(myRect);
            bubblePath = drawBubble(new RectF(0, 0, getWidth(), getHeight()), corner, corner, corner, corner);
            startEnterAnimation();
        }

        public void setup(final Rect viewRect, int screenWidth) {
            this.viewRect = new Rect(viewRect);
            final Rect myRect = new Rect(viewRect);

            final boolean changed = adjustSize(myRect, screenWidth);
            if (!changed) {
                onSetup(myRect);
            } else {
                getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                    @Override
                    public boolean onPreDraw() {
                        onSetup(myRect);
                        getViewTreeObserver().removeOnPreDrawListener(this);
                        return false;
                    }
                });
            }
        }

        public void removeNow() {
            if (getParent() != null) {
                final ViewGroup parent = ((ViewGroup) getParent());
                parent.removeView(TooltipView.this);
            }
        }

        public void closeNow() {
            removeNow();
        }

        public void setDistanceWithView(int distanceWithView) {
            this.distanceWithView = distanceWithView;
        }

        public void setBorderPaint(Paint borderPaint) {
            this.borderPaint = borderPaint;
            postInvalidate();
        }
    }

    public static class MyContext {
        private Fragment fragment;
        private Context context;
        private Activity activity;

        public MyContext(Activity activity) {
            this.activity = activity;
        }

        public MyContext(Fragment fragment) {
            this.fragment = fragment;
        }

        public MyContext(Context context) {
            this.context = context;
        }

        public Context getContext() {
            if (activity != null) {
                return activity;
            } else {
                return ((Context) fragment.getActivity());
            }
        }

        public Activity getActivity() {
            if (activity != null) {
                return activity;
            } else {
                return fragment.getActivity();
            }
        }


        public Window getWindow() {
            if (activity != null) {
                return activity.getWindow();
            } else {
                if (fragment instanceof DialogFragment) {
                    return ((DialogFragment) fragment).getDialog().getWindow();
                }
                return fragment.getActivity().getWindow();
            }
        }
    }
}

