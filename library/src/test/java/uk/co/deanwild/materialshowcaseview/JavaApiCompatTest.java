package uk.co.deanwild.materialshowcaseview;

import static com.google.common.truth.Truth.assertThat;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import uk.co.deanwild.materialshowcaseview.shape.CircleShape;
import uk.co.deanwild.materialshowcaseview.shape.NoShape;
import uk.co.deanwild.materialshowcaseview.shape.OvalShape;
import uk.co.deanwild.materialshowcaseview.shape.RectangleShape;
import uk.co.deanwild.materialshowcaseview.shape.Shape;
import uk.co.deanwild.materialshowcaseview.target.Target;
import uk.co.deanwild.materialshowcaseview.target.ViewTarget;

/**
 * Source-compatibility guard: exercises the 1.x public API exactly as Java callers write it.
 * If this file stops compiling, a public API has been broken.
 */
@RunWith(AndroidJUnit4.class)
public class JavaApiCompatTest {

    @Test
    public void builderApi() {
        TestScreen screen = new TestScreen();
        Activity activity = screen.getActivity();
        View target = screen.getMiddle();

        MaterialShowcaseView.Builder builder = new MaterialShowcaseView.Builder(activity)
                .setTarget(target)
                .setGravity(Gravity.CENTER)
                .setSequence(false)
                .setDismissText("GOT IT")
                .setDismissText(android.R.string.ok)
                .setDismissStyle(Typeface.DEFAULT_BOLD)
                .setSkipText("SKIP")
                .setSkipText(android.R.string.cancel)
                .setSkipStyle(Typeface.DEFAULT)
                .setContentText("content")
                .setContentText(android.R.string.ok)
                .setTitleText("title")
                .setTitleText(android.R.string.ok)
                .setTargetTouchable(true)
                .setDismissOnTargetTouch(true)
                .setDismissOnTouch(false)
                .setMaskColour(Color.BLACK)
                .setTitleTextColor(Color.WHITE)
                .setContentTextColor(Color.WHITE)
                .setDismissTextColor(Color.WHITE)
                .setDelay(0)
                .setFadeDuration(300)
                .setListener(new IShowcaseListener() {
                    @Override
                    public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {
                    }

                    @Override
                    public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
                    }
                })
                .singleUse("java-compat")
                .setShapePadding(MaterialShowcaseView.DEFAULT_SHAPE_PADDING)
                .setTooltipMargin(MaterialShowcaseView.DEFAULT_TOOLTIP_MARGIN)
                .withCircleShape()
                .withOvalShape()
                .withoutShape()
                .withRectangleShape()
                .withRectangleShape(true)
                .renderOverNavigationBar()
                .useFadeAnimation()
                .setShape(new CircleShape(10))
                .setToolTip(ShowcaseTooltip.build(activity).text("tip"));

        MaterialShowcaseView view = builder.build();
        boolean fired = view.hasFired();
        assertThat(fired).isFalse();

        view.setTarget(new ViewTarget(target));
        view.setTarget(new ViewTarget(target.getId(), activity));
        view.setTarget(Target.NONE);
        view.setGravity(Gravity.BOTTOM);
        view.setShape(new NoShape());
        view.setAnimationFactory(new FadeAnimationFactory());
        view.setAnimationFactory(new CircularRevealAnimationFactory());
        view.setConfig(new ShowcaseConfig());
        view.addShowcaseListener(new IShowcaseListener() {
            @Override
            public void onShowcaseDisplayed(MaterialShowcaseView showcaseView) {
            }

            @Override
            public void onShowcaseDismissed(MaterialShowcaseView showcaseView) {
            }
        });
        view.removeShowcaseListener(new MaterialShowcaseSequence(activity));
        int navBar = view.getSoftButtonsBarSizePort();
        assertThat(navBar).isAtLeast(0);
        view.resetSingleUse();

        MaterialShowcaseView.resetSingleUse(activity, "java-compat");
        MaterialShowcaseView.resetAll(activity);

        // Builder.show() returns the view; MaterialShowcaseView.show(Activity) returns a boolean.
        MaterialShowcaseView shown = new MaterialShowcaseView.Builder(activity).setTarget(target).show();
        shown.hide();
        shown.skip();
        shown.removeFromWindow();
    }

    @Test
    public void viewConstructorsAndSubclassing() {
        Activity activity = new TestScreen().getActivity();
        new MaterialShowcaseView(activity);
        new MaterialShowcaseView(activity, null);
        new MaterialShowcaseView(activity, null, 0);
        new MaterialShowcaseView(activity, null, 0, 0);
        MyShowcaseView custom = new MyShowcaseView(activity);
        custom.fadeIn();
        custom.animateOut();
        custom.onClick(custom);
    }

    static class MyShowcaseView extends MaterialShowcaseView {
        MyShowcaseView(Context context) {
            super(context);
        }

        @Override
        public void hide() {
            super.hide();
        }
    }

    @Test
    public void sequenceApi() {
        TestScreen screen = new TestScreen();
        Activity activity = screen.getActivity();

        MaterialShowcaseSequence sequence = new MaterialShowcaseSequence(activity);
        MaterialShowcaseSequence single = new MaterialShowcaseSequence(activity, "java-seq");

        ShowcaseConfig config = new ShowcaseConfig();
        config.setDelay(500);
        config.setMaskColor(Color.BLACK);
        config.setContentTextColor(Color.WHITE);
        config.setDismissTextColor(Color.WHITE);
        config.setDismissTextStyle(Typeface.DEFAULT);
        config.setFadeDuration(100);
        config.setShape(new RectangleShape(10, 10));
        config.setShapePadding(5);
        config.setRenderOverNavigationBar(true);
        long delay = config.getDelay();
        int mask = config.getMaskColor();
        int content = config.getContentTextColor();
        int dismiss = config.getDismissTextColor();
        Typeface style = config.getDismissTextStyle();
        long fade = config.getFadeDuration();
        Shape shape = config.getShape();
        int padding = config.getShapePadding();
        Boolean overNav = config.getRenderOverNavigationBar();
        String defaultMask = ShowcaseConfig.DEFAULT_MASK_COLOUR;

        sequence.setConfig(config);
        MaterialShowcaseSequence chained = sequence
                .addSequenceItem(screen.getTop(), "content", "GOT IT")
                .addSequenceItem(screen.getMiddle(), "title", "content", "GOT IT")
                .addSequenceItem(new MaterialShowcaseView.Builder(activity).setTarget(screen.getBottom()).build())
                .singleUse("java-seq-2");

        sequence.setOnItemShownListener(new MaterialShowcaseSequence.OnSequenceItemShownListener() {
            @Override
            public void onShow(MaterialShowcaseView itemView, int position) {
            }
        });
        sequence.setOnItemDismissedListener(new MaterialShowcaseSequence.OnSequenceItemDismissedListener() {
            @Override
            public void onDismiss(MaterialShowcaseView itemView, int position) {
            }
        });

        IDetachedListener asListener = sequence;
        boolean fired = single.hasFired();
        sequence.start();
    }

    @Test
    public void prefsManagerApi() {
        Activity activity = new TestScreen().getActivity();
        PrefsManager prefs = new PrefsManager(activity, "java-prefs");
        prefs.resetShowcase();
        prefs.close();
        PrefsManager.resetAll(activity);
        int never = PrefsManager.SEQUENCE_NEVER_STARTED;
        int finished = PrefsManager.SEQUENCE_FINISHED;
        assertThat(never).isEqualTo(0);
        assertThat(finished).isEqualTo(-1);
    }

    @Test
    public void shapesAndTargets() {
        Rect rect = new Rect(0, 0, 10, 10);
        Target target = new Target() {
            @Override
            public Point getPoint() {
                return new Point(5, 5);
            }

            @Override
            public Rect getBounds() {
                return new Rect(0, 0, 10, 10);
            }
        };

        CircleShape circle = new CircleShape();
        circle = new CircleShape(5);
        circle = new CircleShape(rect);
        circle = new CircleShape(target);
        circle.setAdjustToTarget(false);
        boolean adjust = circle.isAdjustToTarget();
        circle.setRadius(3);
        int radius = circle.getRadius();
        int preferred = CircleShape.getPreferredRadius(rect);

        OvalShape oval = new OvalShape();
        oval = new OvalShape(5);
        oval = new OvalShape(rect);
        oval = new OvalShape(target);
        oval.setAdjustToTarget(true);
        adjust = oval.isAdjustToTarget();
        oval.setRadius(4);
        radius = oval.getRadius();
        preferred = OvalShape.getPreferredRadius(rect);

        RectangleShape rectangle = new RectangleShape(10, 10);
        rectangle = new RectangleShape(rect);
        rectangle = new RectangleShape(rect, true);
        rectangle.setAdjustToTarget(false);
        adjust = rectangle.isAdjustToTarget();

        Shape[] shapes = {circle, oval, rectangle, new NoShape(), new MyShape(), new MyCircle()};
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        for (Shape s : shapes) {
            s.setPadding(2);
            s.updateTarget(target);
            int w = s.getWidth();
            int h = s.getHeight();
            int total = s.getTotalRadius();
            s.draw(canvas, paint, 5, 5);
        }

        Point none = Target.NONE.getPoint();
        Rect noneBounds = Target.NONE.getBounds();
    }

    static class MyShape implements Shape {
        @Override
        public void draw(Canvas canvas, Paint paint, int x, int y) {
        }

        @Override
        public int getWidth() {
            return 0;
        }

        @Override
        public int getHeight() {
            return 0;
        }

        @Override
        public void updateTarget(Target target) {
        }

        @Override
        public int getTotalRadius() {
            return 0;
        }

        @Override
        public void setPadding(int padding) {
        }
    }

    static class MyCircle extends CircleShape {
        @Override
        public void draw(Canvas canvas, Paint paint, int x, int y) {
            super.draw(canvas, paint, x, y);
        }
    }

    @Test
    public void viewTargetApi() {
        TestScreen screen = new TestScreen();
        ViewTarget target = new ViewTarget(screen.getMiddle());
        View v = target.getView();
        Point p = target.getPoint();
        Rect r = target.getBounds();
        assertThat(v).isSameInstanceAs(screen.getMiddle());
    }

    @Test
    public void animationFactoryApi() {
        IAnimationFactory custom = new IAnimationFactory() {
            @Override
            public void animateInView(View target, Point point, long duration, AnimationStartListener listener) {
                listener.onAnimationStart();
            }

            @Override
            public void animateOutView(View target, Point point, long duration, AnimationEndListener listener) {
                listener.onAnimationEnd();
            }

            @Override
            public void animateTargetToPoint(MaterialShowcaseView showcaseView, Point point) {
            }
        };
        IAnimationFactory.AnimationStartListener start = new IAnimationFactory.AnimationStartListener() {
            @Override
            public void onAnimationStart() {
            }
        };
        IAnimationFactory.AnimationEndListener end = new IAnimationFactory.AnimationEndListener() {
            @Override
            public void onAnimationEnd() {
            }
        };
    }

    @Test
    public void tooltipApi() {
        TestScreen screen = new TestScreen();
        Activity activity = screen.getActivity();

        ShowcaseTooltip tooltip = ShowcaseTooltip.build(activity)
                .position(ShowcaseTooltip.Position.TOP)
                .position(ShowcaseTooltip.Position.BOTTOM)
                .position(ShowcaseTooltip.Position.LEFT)
                .position(ShowcaseTooltip.Position.RIGHT)
                .customView(new View(activity))
                .arrowWidth(10)
                .arrowHeight(10)
                .arrowSourceMargin(1)
                .arrowTargetMargin(1)
                .align(ShowcaseTooltip.ALIGN.START)
                .align(ShowcaseTooltip.ALIGN.CENTER)
                .align(ShowcaseTooltip.ALIGN.END)
                .color(Color.BLACK)
                .color(new Paint())
                .onDisplay(new ShowcaseTooltip.ListenerDisplay() {
                    @Override
                    public void onDisplay(View view) {
                    }
                })
                .padding(1, 2, 3, 4)
                .animation(new ShowcaseTooltip.FadeTooltipAnimation())
                .animation(new ShowcaseTooltip.FadeTooltipAnimation(200))
                .animation(new ShowcaseTooltip.TooltipAnimation() {
                    @Override
                    public void animateEnter(View view, Animator.AnimatorListener animatorListener) {
                    }

                    @Override
                    public void animateExit(View view, Animator.AnimatorListener animatorListener) {
                    }
                })
                .text("text")
                .text(android.R.string.ok)
                .corner(5)
                .textColor(Color.WHITE)
                .textTypeFace(Typeface.DEFAULT)
                .textSize(TypedValue.COMPLEX_UNIT_SP, 14f)
                .setTextGravity(Gravity.CENTER)
                .distanceWithView(4)
                .border(Color.RED, 2f);

        tooltip.configureTarget(screen.getRoot(), screen.getMiddle());
        tooltip.customView(screen.getTop().getId());
        ShowcaseTooltip.TooltipView tv = tooltip.show(0);

        ShowcaseTooltip.TooltipView view = new ShowcaseTooltip.TooltipView(activity);
        view.setCustomView(new View(activity));
        view.setColor(Color.BLACK);
        view.setPaint(new Paint());
        view.setPosition(ShowcaseTooltip.Position.TOP);
        view.setAlign(ShowcaseTooltip.ALIGN.CENTER);
        view.setText("text");
        view.setText(android.R.string.ok);
        view.setTextColor(Color.WHITE);
        view.setArrowHeight(view.getArrowHeight());
        view.setArrowWidth(view.getArrowWidth());
        view.setArrowSourceMargin(view.getArrowSourceMargin());
        view.setArrowTargetMargin(view.getArrowTargetMargin());
        view.setTextTypeFace(Typeface.DEFAULT);
        view.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
        view.setTextGravity(Gravity.START);
        view.setCorner(3);
        view.setListenerDisplay(null);
        view.setTooltipAnimation(new ShowcaseTooltip.FadeTooltipAnimation());
        view.setDistanceWithView(2);
        view.setBorderPaint(new Paint());
        Rect rect = new Rect(0, 0, 100, 100);
        view.setupPosition(rect);
        boolean adjusted = view.adjustSize(rect, 1000);
        view.setup(rect, 1000);
        view.removeNow();
        view.closeNow();

        ShowcaseTooltip.MyContext ctx = new ShowcaseTooltip.MyContext(activity);
        Context c = ctx.getContext();
        Activity a = ctx.getActivity();
        android.view.Window w = ctx.getWindow();
        new ShowcaseTooltip.MyContext((Context) activity);
        new ShowcaseTooltip.MyContext(new android.app.Fragment());
    }
}
