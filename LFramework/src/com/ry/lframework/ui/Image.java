package com.ry.lframework.ui;

import android.content.Context;

public class Image extends DisplayObejct {

	public Image(Context c, String url) {
		super(c, ViewLayer.DrawTypeIMAGE, url, null);
	}

	public String getSrc(){ return attrs.get("src", ""); }
	public void setSrc(String value){ attrs.set("src", value); updateView(); }
}
