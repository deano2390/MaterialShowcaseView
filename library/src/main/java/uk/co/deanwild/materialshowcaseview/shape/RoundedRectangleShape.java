package uk.co.deanwild.materialshowcaseview.shape;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

import uk.co.deanwild.materialshowcaseview.target.Target;

public class RoundedRectangleShape
		implements Shape {

	private int width = 0;
	private int height = 0;

	private int xRadius = 0;
	private int yRadius = 0;
	private boolean adjustToTarget = true;

	private Rect rect;

	public RoundedRectangleShape(int width, int height, int xRadius, int yRadius) {
		this.width = width;
		this.height = height;
		this.xRadius = xRadius;
		this.yRadius = yRadius;
		init();
	}

	public RoundedRectangleShape(Rect bounds, int xRadius, int yRadius) {
		this.height = bounds.height();
		this.width = bounds.width();
		this.xRadius = xRadius;
		this.yRadius = yRadius;
		init();
	}

	public boolean isAdjustToTarget() {
		return adjustToTarget;
	}

	public void setAdjustToTarget(boolean adjustToTarget) {
		this.adjustToTarget = adjustToTarget;
	}

	private void init() {
		rect = new Rect(- width / 2, - height / 2, width / 2, height / 2);
	}

	@Override
	public void draw(Canvas canvas, Paint paint, int x, int y, int padding) {
		if (!rect.isEmpty()) {
			canvas.drawRoundRect(
					new RectF(
					rect.left + x - padding,
					rect.top + y - padding,
					rect.right + x + padding,
					rect.bottom + y + padding
					),
					xRadius,
					yRadius,
					paint
			);
		}
	}

	@Override
	public void updateTarget(Target target) {
		if (adjustToTarget) {
			Rect bounds = target.getBounds();
			height = bounds.height();
			width = bounds.width();
			init();
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
