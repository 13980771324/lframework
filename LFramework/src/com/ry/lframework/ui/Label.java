package com.ry.lframework.ui;

import android.content.Context;

public class Label extends DisplayObejct {

	public Label(Context c, String url) {
		super(c, ViewLayer.DrawTypeTEXT, url, null);
	}

	public int getFontSize(){ return attrs.get("fontSize", 20); }
	public void setFontSize(int value){ attrs.set("fontSize", value); updateView(); }
	public int getLimitWidth(){ return attrs.get("limitWidth", 1024); }
	public void setLimitWidth(int value){ attrs.set("limitWidth", value); updateView(); }
	public int getText(){ return attrs.get("text", ""); }
	public void setText(String value){ attrs.set("text", value); updateView(); }

}
