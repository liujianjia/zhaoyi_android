package com.android.view.animation.textsurface.animations;

import com.android.view.animation.textsurface.contants.TYPE;
import com.android.view.animation.textsurface.interfaces.ISurfaceAnimation;

/**
 * Created by Eugene Levenetc.
 */
public class Sequential extends AnimationsSet {
	public Sequential(ISurfaceAnimation... animations) {
		super(TYPE.SEQUENTIAL, animations);
	}
}
