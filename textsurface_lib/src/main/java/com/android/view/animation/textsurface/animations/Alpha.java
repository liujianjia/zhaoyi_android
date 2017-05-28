package com.android.view.animation.textsurface.animations;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.support.annotation.Nullable;

import com.android.view.animation.textsurface.Text;
import com.android.view.animation.textsurface.interfaces.IEndListener;
import com.android.view.animation.textsurface.utils.AnimatorEndListener;

/**
 * Created by Eugene Levenetc.
 */
public class Alpha extends AbstractSurfaceAnimation {

	private int to;
	private int from;

	public static Alpha hide(Text text, int duration) {
		return new Alpha(text, duration, 255, 0);
	}

	public static Alpha show(Text text, int duration) {
		return new Alpha(text, duration, 0, 255);
	}

	public Alpha(Text text, int duration, int from, int to) {
		super(text, duration);
		this.from = from;
		this.to = to;
	}

	@Override
	public void start(@Nullable final IEndListener listener) {
		ValueAnimator animator = ValueAnimator.ofInt(from, to);
		animator.setDuration(duration);
		animator.addUpdateListener(this);

		animator.addListener(new AnimatorEndListener() {
			@Override
			public void onAnimationEnd(Animator animation) {
				if (listener != null)
					listener.onAnimationEnd(Alpha.this);
			}
		});

		animator.start();
	}

	@Override
	public void onAnimationUpdate(ValueAnimator animation) {
		super.onAnimationUpdate(animation);
		text.setAlpha((Integer) animation.getAnimatedValue());
	}
}
