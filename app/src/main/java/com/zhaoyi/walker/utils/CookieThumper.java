package com.zhaoyi.walker.utils;

import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.android.view.animation.textsurface.Text;
import com.android.view.animation.textsurface.TextBuilder;
import com.android.view.animation.textsurface.TextSurface;
import com.android.view.animation.textsurface.animations.Delay;
import com.android.view.animation.textsurface.animations.Parallel;
import com.android.view.animation.textsurface.animations.Sequential;
import com.android.view.animation.textsurface.animations.ShapeReveal;
import com.android.view.animation.textsurface.animations.SideCut;
import com.android.view.animation.textsurface.animations.Slide;
import com.android.view.animation.textsurface.animations.TransSurface;
import com.android.view.animation.textsurface.contants.Align;
import com.android.view.animation.textsurface.contants.Side;

public class CookieThumper {

	public static void play(TextSurface textSurface, AssetManager assetManager) {

		final Typeface robotoBlack = Typeface.createFromAsset(assetManager,
				"fonts/Roboto-Black.ttf");
		Paint paint = new Paint();
		paint.setAntiAlias(true);
		paint.setTypeface(robotoBlack);

		Text textDaai = TextBuilder
				.create("找   医")
				.setPaint(paint)
				.setSize(38)
				.setAlpha(0)
				.setColor(Color.parseColor("#20d2c3"))
				.setPosition(Align.SURFACE_CENTER)
				.build();
		
		Text textDaaiAnies = TextBuilder
				.create("你的专属挂号APP")
				.setPaint(paint)
				.setSize(20)
				.setAlpha(0)
				.setColor(Color.parseColor("#20d2c3"))
				.setPosition(Align.BOTTOM_OF | Align.CENTER_OF, textDaai)
				.build();

//		textSurface.play(new Sequential(ShapeReveal.create(textDaai, 600,SideCut.show(Side.CENTER), false),
//				new Parallel(ShapeReveal.create(textDaai, 600, SideCut.hide(Side.LEFT), false),
//				new Sequential(Delay.duration(300), ShapeReveal.create(textDaai, 600, SideCut.show(Side.LEFT), false))),
//		        new Parallel(TransSurface.toCenter(textDaaiAnies, 1000), Slide.showFrom(Side.TOP, textDaaiAnies, 1000))
//		        )
//		);
		
		textSurface.play(
				new Sequential(
						ShapeReveal.create(textDaai, 600, SideCut.show(Side.LEFT), false),
						new Parallel(ShapeReveal.create(textDaai, 600, SideCut.hide(Side.LEFT), false), new Sequential(Delay.duration(300), ShapeReveal.create(textDaai, 600, SideCut.show(Side.LEFT), false))),
						Delay.duration(300),
						new Parallel(TransSurface.toCenter(textDaaiAnies, 1000), Slide.showFrom(Side.TOP, textDaaiAnies, 1000))
				)
		);
		
		
	}

}
