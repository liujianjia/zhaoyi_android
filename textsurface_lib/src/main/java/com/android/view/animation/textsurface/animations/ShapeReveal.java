package com.android.view.animation.textsurface.animations;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.animation.FastOutSlowInInterpolator;

import com.android.view.animation.textsurface.Text;
import com.android.view.animation.textsurface.TextSurface;
import com.android.view.animation.textsurface.interfaces.IEndListener;
import com.android.view.animation.textsurface.interfaces.ISurfaceAnimation;
import com.android.view.animation.textsurface.interfaces.ITextEffect;
import com.android.view.animation.textsurface.utils.Utils;

/**
 * Created by Eugene Levenetc.
 */
public class ShapeReveal implements ITextEffect, ValueAnimator.AnimatorUpdateListener {

	private final Text text;
	private final int duration;
	private TextSurface textSurface;
	private IRevealShape revealShape;
	private boolean hideOnEnd;

	public static ShapeReveal create(Text text, int duration, IRevealShape revealShape, boolean hideOnEnd) {
		return new ShapeReveal(text, duration, revealShape, hideOnEnd);
	}

	private ShapeReveal(Text text, int duration, IRevealShape revealShape, boolean hideOnEnd) {
		this.text = text;
		this.duration = duration;
		this.revealShape = revealShape;
		this.hideOnEnd = hideOnEnd;
		revealShape.setText(text);
	}

	@Override public void apply(Canvas canvas, String textValue, float x, float y, Paint paint) {
		revealShape.clip(canvas, textValue, x, y, paint);
	}

	@Override public void setInitValues(@NonNull Text text) {
		if (revealShape.isToShow()) text.setAlpha(0);
	}

	@NonNull @Override public Text getText() {
		return text;
	}

	@Override public void onStart() {
		text.addEffect(this);
	}

	@Override public void start(@Nullable final IEndListener listener) {

		text.setAlpha(255);
		ValueAnimator animator = revealShape.getAnimator();
		animator.setInterpolator(new FastOutSlowInInterpolator());
		Utils.addEndListener(this, animator, new IEndListener() {
			@Override public void onAnimationEnd(ISurfaceAnimation animation) {
				text.removeEffect(ShapeReveal.this);
				if (hideOnEnd) text.setAlpha(0);
				if (listener != null) listener.onAnimationEnd(ShapeReveal.this);
			}
		});
		animator.setDuration(duration);
		animator.start();
	}

	@Override public void setTextSurface(@NonNull TextSurface textSurface) {
		revealShape.setTextSurface(textSurface);
		this.textSurface = textSurface;
	}

	@Override public long getDuration() {
		return 0;
	}

	@Override public void onAnimationUpdate(ValueAnimator animation) {
		textSurface.invalidate();
	}

	public interface IRevealShape {
		void setText(Text text);

		void setTextSurface(TextSurface textSurface);

		ValueAnimator getAnimator();

		void clip(Canvas canvas, String textValue, float x, float y, Paint paint);

		boolean isToShow();
	}
}
