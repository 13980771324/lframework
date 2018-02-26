package com.ry.lframework.ui;

import com.ry.lframework.LCallBack;
import com.ry.lframework.LFramework;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.TranslateAnimation;

@SuppressLint({ "ClickableViewAccessibility", "NewApi" })
public class Window extends Layout implements AnimationListener {
	private static final int STATE_NONE = 0;
	private static final int STATE_OPEN = 1;
	private static final int STATE_OPENING = 2;
	private static final int STATE_CLOSEING = 3;
	private int state = 0;
	public Window(Context c) {
		super(c, String.format("color=%d&width=%d&height=%d&frontDraw='drawRect %d,0,0,%d,%d'",0xFFEEEEEE,LFramework.displayWidth,LFramework.displayHeight,0xFF000000,LFramework.displayWidth,LFramework.displayHeight));
		bindLayer.setOnTouchListener(this);
		setStateBarVisile(true);
		
//        Image img = new Image(c, String.format("color=%d&width=100&height=100&x=10&y=10&src='add.png'&autoSize=true", 0xFF00FF00));
//        addChild(img);
//        img.setListener(new LCallBack(0){
//        	@Override
//        	public void onCall(String name, Object args) {
//        		System.out.println(name);
//        		if(args.equals(2))
//        			new Window(LFramework.getInstance()).open(true);
//        	}
//        });
//        Image img2 = new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00));
//        addChild(img2);
//        img2.setListener(new LCallBack(0){
//        	@Override
//        	public void onCall(String name, Object args) {
//        		System.out.println(name + " close");
//        		if(args.equals(2))
//        			close(true);
//        	}
//        });
        
//        List list = new List(c, "width=100&height=100");
//        list.setBackgroundColor(0xFF000088);
//        list.push(new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00)));
//        list.push(new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00)));
//        list.push(new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00)));
//        list.push(new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00)));
//        list.push(new Image(c, String.format("color=%d&width=100&height=100&x=10&y=100&src='add.png'&autoSize=true", 0xFF00FF00)));
//        addChild(list);
	}

	public boolean onTouch(View target, MotionEvent event) {
		if(listener != null)
			return super.onTouch(target, event);
		return true;
	}
	
	public void front(){
		if(parent != null)
			parent.changeChildIndex(this, parent.numOfChildren());
	}
	
	public void setStateBarVisile(boolean value){
		android.view.Window w = LFramework.getInstance().getWindow();
		if (Build.VERSION.SDK_INT >= 21) {
			w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
			if(value){
				w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
				w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
				w.setStatusBarColor(Color.TRANSPARENT);
				w.setNavigationBarColor(Color.TRANSPARENT);
			}else{
				w.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
			}
		} else {
			w.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
			w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
			if(value){
				w.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION); 
			}else{
				w.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN); 
			}
		}
	}
	
	public void open(boolean hasEffect){
		if(state != STATE_NONE)
			return;
		LFramework.getInstance().getScene().getWindows().addChild(this);
		if(hasEffect){
			state = STATE_OPENING;
			TranslateAnimation t = new TranslateAnimation(0, LFramework.displayWidth, 0, 0, 0, 0, 0, 0);
			t.setDuration(300);
			t.setAnimationListener(this);
			bindLayer.startAnimation(t);
		}else{
			state = STATE_OPEN;
			onCreate();
		}
	}
	
	public void close(boolean hasEffect){
		if(state != STATE_OPEN)
			return;
		if(hasEffect){
			state = STATE_CLOSEING;
			TranslateAnimation t = new TranslateAnimation(0, 0, 0, LFramework.displayWidth, 0, 0, 0, 0);
			t.setDuration(300);
			t.setAnimationListener(this);
			bindLayer.startAnimation(t);
		}else{
			state = STATE_NONE;
			onCLose();
			LFramework.getInstance().getScene().getWindows().removeChild(this);
		}
	}

	private void onCLose() {
		if(listener != null)
			listener.onCall("WindowClose", this);
	}

	private void onCreate() {
		if(listener != null)
			listener.onCall("WindowCreate", this);
	}
	
	@Override
	public void onAnimationEnd(Animation ani) {
		if(state == STATE_OPENING){
			state = STATE_OPEN;
			onCreate();
		}else if(state == STATE_CLOSEING){
			state = STATE_NONE;
			onCLose();
			LFramework.getInstance().getScene().getWindows().removeChild(this);
		}
	}

	@Override
	public void onAnimationRepeat(Animation ani) {}
	@Override
	public void onAnimationStart(Animation ani) {}
	
}
