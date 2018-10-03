package com.hcpt.taxinear.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.TextView;

public class TextViewFontAwesome extends TextView {

	public TextViewFontAwesome(Context context) {
		super(context);
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				"fonts/fontawesome-webfont.ttf");
		this.setTypeface(face);
	}

	public TextViewFontAwesome(Context context, AttributeSet attrs) {
		super(context, attrs);
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				"fonts/fontawesome-webfont.ttf");
		this.setTypeface(face);
	}

	public TextViewFontAwesome(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		Typeface face = Typeface.createFromAsset(context.getAssets(),
				"fonts/fontawesome-webfont.ttf");
		this.setTypeface(face);
	}

	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

	}
}
