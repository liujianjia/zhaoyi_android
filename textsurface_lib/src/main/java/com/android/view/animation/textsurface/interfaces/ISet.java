package com.android.view.animation.textsurface.interfaces;

import com.android.view.animation.textsurface.contants.TYPE;

import java.util.LinkedList;

/**
 * Created by Eugene Levenetc.
 */
public interface ISet extends ISurfaceAnimation {
	TYPE getType();

	LinkedList<ISurfaceAnimation> getAnimations();
}
