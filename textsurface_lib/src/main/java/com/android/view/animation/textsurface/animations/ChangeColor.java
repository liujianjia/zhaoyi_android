package com.android.view.animation.textsurface.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.android.view.animation.textsurface.Text;
import com.android.view.animation.textsurface.interfaces.IEndListener;
import com.android.view.animation.textsurface.utils.AnimatorEndListener;

/**
 * Created by Eugene Levenetc.
 */
public class ChangeColor extends AbstractSurfaceAnimation {

	private int from;
	private int to;
	final float[] fromTriplet = new float[3];
	final float[] toTriplet = new float[3];
	final float[] hsv = new float[3];

	public static ChangeColor fromTo(Text text, int duration, int from, int to) {
		return new ChangeColor(text, duration, from, to);
	}

	public static ChangeColor to(Text text, int duration, int to) {
		return new ChangeColor(text, duration, -1, to);
	}

	public ChangeColor(Text text, int duration, int from, int to) {
		super(text, duration);
		this.from = from;
		this.to = to;
	}

	@Override public void start(@Nullable final IEndListener listener) {

		if (from == -1) from = text.getPaint().getColor();

		Color.colorToHSV(from, fromTriplet);
		Color.colorToHSV(to, toTriplet);

		ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
		animator.setDuration(duration);
		animator.addUpdateListener(this);

		animator.addListener(new AnimatorEndListener() {
			@Override public void onAnimationEnd(Animator animation) {
				if (listener != null) listener.onAnimationEnd(ChangeColor.this);
			}
		});

		animator.start();
	}

	@Override public void onAnimationUpdate(ValueAnimator animation) {
		hsv[0] = fromTriplet[0] + (toTriplet[0] - fromTriplet[0]) * animation.getAnimatedFraction();
		hsv[1] = fromTriplet[1] + (toTriplet[1] - fromTriplet[1]) * animation.getAnimatedFraction();
		hsv[2] = fromTriplet[2] + (toTriplet[2] - fromTriplet[2]) * animation.getAnimatedFraction();

		text.getPaint().setColor(Color.HSVToColor(hsv));
		super.onAnimationUpdate(animation);
	}
}
