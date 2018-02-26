package com.ry.lframework.ui;

import java.util.ArrayList;
import java.util.HashMap;

import com.ry.lframework.LCallBack;
import com.ry.lframework.ui.ViewLayer.DrawConfig;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

@SuppressLint("ClickableViewAccessibility")
public class DisplayObejct implements OnTouchListener {
	protected DisplayObejct parent;
	private ArrayList<DisplayObejct> children;
	private TouchPoint touchStart,touchMoving,touchEnded;
	protected ViewLayer bindLayer;
	protected DrawConfig attrs;
	protected LCallBack listener;
	public DisplayObejct(Context c,int type,String url,View v){
		
		children = new ArrayList<DisplayObejct>();
		
		HashMap<String, Object> data = parseHashMap(url);
		if(v != null){
			data.put("view", v);
		}
		attrs = new DrawConfig(type,data);
		bindLayer = new ViewLayer(c, attrs);
	}
	public void setListener(LCallBack l){
		listener = l;
		bindLayer.setOnTouchListener(l == null ? null : this);
	}
	public DisplayObejct addChild(DisplayObejct value){
		if(value.parent != null)
			return null;
		value.parent = this;
		bindLayer.addView(value.bindLayer);
		children.add(value);
		return value;
	}
	public DisplayObejct removeChild(DisplayObejct value){
		if(value.parent != this)
			return null;
		value.parent = null;
		bindLayer.removeView(value.bindLayer);
		children.remove(value);
		return value;
	}
	public DisplayObejct removeChildAt(int index){
		if(index >= 0 && index < children.size())
			return removeChild(children.get(index));
		return null;
	}
	public void changeChildIndex(DisplayObejct value,int index){
		if(index < 0)
			index = 0;
		if(index > children.size())
			index = children.size() - 1;
		if(children.remove(value)){
			children.add(index, value);
		}
	}
	public void removeAllChildren(int index){
		while(children.size() > 0)
			removeChild(children.get(0));
	}
	public int numOfChildren(){
		return children.size();
	}
	public void removeFromParent(){
		if(parent != null)
			parent.removeChild(this);
	}
	public View getView(){
		return bindLayer;
	}
	//---------------------base attrs----------------------
	public int getX(){ return attrs.get("x", 0); }
	public void setX(int value){ attrs.set("x", value); updateView(); }
	public int getScaleX(){ return attrs.get("sx", 0); }
	public void setScaleX(int value){ attrs.set("sx", value); updateView(); }
	public int getScaleY(){ return attrs.get("sy", 0); }
	public void setScaleY(int value){ attrs.set("sy", value); updateView(); }
	public int getY(){ return attrs.get("y", 0); }
	public void setY(int value){ attrs.set("y", value); updateView(); }
	public int getWidth(){ return attrs.get("width", 0); }
	public void setWidth(int value){ attrs.set("width", value); updateView(); }
	public int getHeight(){ return attrs.get("height", 0); }
	public void setHeight(int value){ attrs.set("height", value); updateView(); }
	public int getColor(){ return attrs.get("color", 0); }
	public void setColor(int value){ attrs.set("color", value); updateView(); }
	public int getBackgroundColor(){ return attrs.get("bgcolor", 0); }
	public void setBackgroundColor(int value){ attrs.set("bgcolor", value); updateView(); }
	public boolean getAutoSize(){ return attrs.get("autoSize", false); }
	public void setAutoSize(boolean value){ attrs.set("autoSize", value); updateView(); }
	public boolean getVisible(){ return attrs.get("visible", false); }
	public void getVisible(boolean value){ attrs.set("visible", value); updateView(); }
	//---------------------base attrs----------------------
	public TouchPoint getTouchStartPoint(){return touchStart;}
	public TouchPoint getTouchMovePoint(){return touchMoving;}
	public TouchPoint getTouchEndedPoint(){return touchEnded;}
	//-----------------------------------------------------
	protected void updateView() {
		bindLayer.setBackgroundColor((Integer) attrs.get("bgcolor",0));
		bindLayer.updateConfig(attrs);
	}
	private HashMap<String, Object> parseHashMap(String string) {
		HashMap<String, Object> result = new HashMap<String, Object>();
		String[] items = string.split("&");
		for(int i=0;i<items.length;++i){
			String[] kv = items[i].split("=");
			result.put(kv[0], parseValue(kv[1]));
		}
		return result;
	}
	private Object parseValue(String string) {
		if(string.equals("true")) return true;
		if(string.equals("false")) return false;
		if(string.startsWith("\"") && string.endsWith("\"")){
			return string.substring(1,string.length()-1);
		}
		if(string.startsWith("\'") && string.endsWith("\'")){
			return string.substring(1,string.length()-1);
		}
		try{
			if(string.startsWith("0x")){
				return Integer.parseInt(string.substring(2),16);
			}
			return Integer.parseInt(string);
		}catch(Throwable t){
		}
		return string;
	}
	@Override
	public boolean onTouch(View target, MotionEvent event) {
		int eventType = 3;
		switch(event.getAction()){
		case MotionEvent.ACTION_DOWN:
			eventType = 0;
			if(touchStart == null){
				touchStart = new TouchPoint(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}else{
				touchStart.set(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}
			break;
		case MotionEvent.ACTION_MOVE:
			eventType = 1;
			if(touchMoving == null){
				touchMoving = new TouchPoint(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}else{
				touchMoving.set(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}
			break;
		case MotionEvent.ACTION_UP:
			eventType = 2;
			if(touchEnded == null){
				touchEnded = new TouchPoint(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}else{
				touchEnded.set(event.getX(),event.getY(),event.getRawX(),event.getRawY());
			}
			break;
		}
		System.out.println(this);
		if(listener != null){
			listener.onCall("TouchEvent", eventType);
		}
		return true;
	}
}
