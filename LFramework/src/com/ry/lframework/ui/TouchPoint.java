package com.ry.lframework.ui;

import android.annotation.SuppressLint;

@SuppressLint("DefaultLocale")
public class TouchPoint {
	private float x,y,stageX,stageY;
	public TouchPoint(float x,float y,float stageX,float stageY){
		set(x, y, stageX, stageY);
	}

	public float getX(){return x;}
	public float getY(){return y;}
	public float getStageX(){return stageX;}
	public float getStageY(){return stageY;}
	
	public void set(float x,float y,float stageX,float stageY){
		this.x = x;
		this.y = y;
		this.stageX = stageX;
		this.stageY = stageY;
	}
	
	public String toString(){
		return String.format("{local:(%f,%f) world:(%f,%f)}", x,y,stageX,stageY);
	}
}
