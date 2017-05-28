package com.android.view.animation.textsurface.animations;

import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.view.animation.textsurface.Text;
import com.android.view.animation.textsurface.TextSurface;
import com.android.view.animation.textsurface.interfaces.IEndListener;
import com.android.view.animation.textsurface.interfaces.ITextSurfaceAnimation;

/**
 * Created by Eugene Levenetc.
 */
public class AbstractSurfaceAnimation implements ITextSurfaceAnimation, ValueAnimator.AnimatorUpdateListener {

	protected final Text text;
	protected final int duration;
	protected TextSurface textSurface;

	public AbstractSurfaceAnimation(Text text, int duration) {
		this.text = text;
		this.duration = duration;
	}

	@Override public void setInitValues(@NonNull Text text) {

	}

	@NonNull @Override public Text getText() {
		return text;
	}

	@Override public void onStart() {

	}

	@Override public void start(final @Nullable IEndListener listener) {

	}

	@Override public void setTextSurface(@NonNull TextSurface textSurface) {
		this.textSurface = textSurface;
	}

	@Override public long getDuration() {
		return duration;
	}

	@Override public void onAnimationUpdate(ValueAnimator animation) {
		textSurface.invalidate();
	}
}
