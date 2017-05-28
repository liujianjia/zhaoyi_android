package com.android.view.animation.textsurface.interfaces;

import android.support.annotation.NonNull;

import com.android.view.animation.textsurface.Text;

/**
 * Created by Eugene Levenetc.
 */
public interface ITextSurfaceAnimation extends ISurfaceAnimation {

	void setInitValues(@NonNull Text text);

	Text getText();

}