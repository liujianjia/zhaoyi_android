package com.android.view.animation.textsurface.animations;

import com.android.view.animation.textsurface.contants.TYPE;
import com.android.view.animation.textsurface.interfaces.ISurfaceAnimation;

/**
 * Created by Eugene Levenetc.
 */
public class Parallel extends AnimationsSet {
	public Parallel(ISurfaceAnimation... animations) {
		super(TYPE.PARALLEL, animations);
	}
}
