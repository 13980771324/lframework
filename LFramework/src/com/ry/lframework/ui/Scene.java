package com.ry.lframework.ui;

import com.ry.lframework.LFramework;

import android.annotation.SuppressLint;
import android.content.Context;

@SuppressLint("DefaultLocale")
public class Scene extends Layout {
	
	private Layout _desktop,_windows,_popus,_natives;

	public Scene(Context c) {
		super(c, String.format("color=%d&width=%d&height=%d",0xffffffff,LFramework.displayWidth,LFramework.displayHeight));
		
		String config = String.format("width=%d&height=%d",LFramework.displayWidth,LFramework.displayHeight);
		
		_desktop = (Layout) addChild(new Layout(c, config));
		_windows = (Layout) addChild(new Layout(c, config));
		_popus = (Layout) addChild(new Layout(c, config));
		_natives = (Layout) addChild(new Layout(c, config));
	}

	public Layout getDesktop(){ return _desktop; }
	public Layout getWindows(){ return _windows; }
	public Layout getPopus(){ return _popus; }
	public Layout getnatives(){ return _natives; }
}
