package com.ry.lframework.ui;

import java.util.HashMap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.text.Layout.Alignment;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint({ "DrawAllocation", "WrongCall", "NewApi" }) 
public class ViewLayer extends ViewGroup {

	public static final int DrawTypeVIEW = 0;//data=View
	public static final int DrawTypeLAYOUT = 1;//data=null
	public static final int DrawTypeTEXT = 2;//data=text
	public static final int DrawTypeIMAGE = 3;//data=assets/file.png
	
	public static class DrawConfig{
		public int type;
		public HashMap<String, Object> cfg;
		public DrawConfig(int t,HashMap<String, Object> h){
			type = t;
			cfg = h;
		}
		@SuppressWarnings("unchecked")
		public <T> T get(String key,Object def){
			Object result = null;
			if(cfg != null)
				result = cfg.get(key);
			if(result == null)
				result = def;
			return (T) result;
		}
		public void set(String key,Object val){
			cfg.put(key, val);
		}
		public void setData(HashMap<String, Object> data){
			cfg = data;
		}
	}
	
	private DrawConfig config = null;
	private Paint paint = null;
	
	public ViewLayer(Context context,DrawConfig c) {
		super(context);
		setWillNotDraw(false);
		updateConfig(c);
	}
	
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//		measureChildren(widthMeasureSpec, heightMeasureSpec);
//		setMeasuredDimension( getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec),getDefaultSize(getSuggestedMinimumHeight(), heightMeasureSpec));
		measureChildren(widthMeasureSpec, heightMeasureSpec);
	    super.onMeasure(widthMeasureSpec, heightMeasureSpec);  
	    
	    paint = new Paint();
		if(config != null){
			int w = config.get("width", 1);
			int h = config.get("height", 1);
			if(config.type == DrawTypeTEXT)
				paint = new TextPaint();
			int[] size = onMasureNow(paint);
			if(size != null){
				w = size[0];
				h = size[1];
			}
	        setMeasuredDimension(w, h);
	        return;
		}

        setMeasuredDimension(1, 1);
	}

	private int[] onMasureNow(Paint p) {
		if(config == null)
			return null;
		boolean autoSize = config.get("autoSize", false);
		int[] size = null;
		switch (config.type) {
		case DrawTypeLAYOUT:
			size = onMeasureLayoutAndPaint(config,p);
			break;
		case DrawTypeTEXT:
			size = onMeasureTextAndPaint(config,p);
			break;
		case DrawTypeIMAGE:
			size = onMeasureImageAndPaint(config,p);
			break;
		}
		if(autoSize && size != null){
			config.set("width", size[0]);
			config.set("height", size[1]);
		}
		return size;
	}

	private int[] onMeasureImageAndPaint(DrawConfig c, Paint p) {
		String	src = c.get("src", "");
		Bitmap bitmap = BitmapCache.getInstance().getBitmap(src);
		return new int[]{bitmap.getWidth(),bitmap.getHeight()};
	}

	private int[] onMeasureTextAndPaint(DrawConfig c, Paint p) {
		String	text = c.get("text", "");
		Integer fontSize = c.get("fontSize", 20);
		Integer color = c.get("color", 0);
		Integer	limitWidth = c.get("limitWidth", 1024);
		p.setColor(color);
		p.setTextSize(fontSize);
		p.setDither(true);
		p.setAntiAlias(true);
		p.setTextAlign(Paint.Align.LEFT);
		StaticLayout staticLayout = new StaticLayout(text, (TextPaint) p, limitWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
		int w = 1;
		int h = staticLayout.getHeight();
		int max = staticLayout.getLineCount();
		for(int i=0;i<max;++i){
			int cw = (int) staticLayout.getLineWidth(i);
			if(cw > w){
				w = cw;
			}
		}
		return new int[]{w,h};
	}

	private int[] onMeasureLayoutAndPaint(DrawConfig c, Paint p) {
		Integer color = c.get("color", 0);
		p.setStyle(Style.FILL);
		p.setColor(color);
		return null;
	}

	@SuppressLint("NewApi")
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
	    int childCount = getChildCount();        
	    for ( int i = 0; i < childCount; i++ ) {
	    	View childView = getChildAt(i);
	        int width  = childView.getMeasuredWidth();
	        int height = childView.getMeasuredHeight();
	        int x = (int) childView.getX();
	        int y = (int) childView.getY();
	        if(childView instanceof ViewLayer){
	        	ViewLayer v = (ViewLayer) childView;
	        	x = v.config.get("x", 0);
	        	y = v.config.get("y", 0);
	        }
	        
	    	childView.layout(x,y,width+x, height+y);
	    }
	}

	
	public DrawConfig getConfig(){
		return config;
	}
	
	public void updateConfig(DrawConfig c){
		if(c == null){
			config = null;
			invalidate();
			return;
		}
		if(c.cfg == null)
			return;
		if(config != c)
			config = c;
		if(config.type == DrawTypeVIEW){
			Object v = config.cfg.get("view");
			if(v != null && v instanceof View){
				View view = (View)v;
				if(view.getParent() == null)
					addView((View) v);
			}
		}

		boolean visible = config.get("visible", true);
		setVisibility(visible ? View.VISIBLE : View.GONE);
		if(!visible)
			return;

		if(config.get("autoSize", false)){
			onMasureNow(config.type == DrawTypeTEXT ? new TextPaint() : new Paint());
		}
		

		int sx = config.get("sx", 255);
		int sy = config.get("sy", 255);
		setScaleX(sx / 255.0f);
		setScaleY(sy / 255.0f);
		
		requestLayout();
		invalidate();
	}
	
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(config != null){
			onDrawBackground(canvas,config);
			switch (config.type) {
			case DrawTypeLAYOUT:
				onDrawLayout(canvas,config);
				break;
			case DrawTypeTEXT:
				onDrawText(canvas,config);
				break;
			case DrawTypeIMAGE:
				onDrawImage(canvas,config);
				break;
			}
			onDrawFront(canvas,config);
		}
	}

	//控件前景画  --
	private void onDrawFront(Canvas canvas, DrawConfig cfg) {
		onDrawGraphics(canvas,(String) cfg.get("frontDraw", ""));
	}

	//控件背景画  --
	private void onDrawBackground(Canvas canvas, DrawConfig cfg) {
		onDrawGraphics(canvas,(String) cfg.get("backgroundDraw", ""));
	}
	private static int argsAt(String[] args,int index){
		try{
			if(index >= 0 && index < args.length)
				return Integer.parseInt(args[index]);
		}catch(Throwable t){}
		return 0;
	}
	private void onDrawGraphics(Canvas canvas,String gcode) {
		if(gcode.length() == 0)
			return;
		String[] fp = gcode.split(" ");
		String[] args = fp.length > 0 ? fp[1].split(",") : new String[]{};
		
		Paint paint = new Paint();
		paint.setColor(argsAt(args,0));
		
		if(fp[0].equals("drawRect")){
			paint.setStyle(Style.STROKE);
			int x = argsAt(args,1);int y=argsAt(args,2);int w=argsAt(args,3);int h=argsAt(args,4);
			canvas.drawRect(x,y,x+w,y+h, paint);
		}else if(fp[0].equals("fileRect")){
			paint.setStyle(Style.FILL);
			int x = argsAt(args,1);int y=argsAt(args,2);int w=argsAt(args,3);int h=argsAt(args,4);
			canvas.drawRect(x,y,x+w,y+h, paint);
		}else if(fp[0].equals("drawRoundRect")){
			paint.setStyle(Style.STROKE);
			paint.setAntiAlias(true);
			int x = argsAt(args,1);int y=argsAt(args,2);int w=argsAt(args,3);int h=argsAt(args,4);int e1=argsAt(args,5);int e2=argsAt(args,6);
			canvas.drawRoundRect(new RectF(x, y, x+w, y+h), e1, e2, paint);
		}else if(fp[0].equals("fillRoundRect")){
			paint.setStyle(Style.FILL);
			paint.setAntiAlias(true);
			int x = argsAt(args,1);int y=argsAt(args,2);int w=argsAt(args,3);int h=argsAt(args,4);int e1=argsAt(args,5);int e2=argsAt(args,6);
			canvas.drawRoundRect(new RectF(x, y, x+w, y+h), e1, e2, paint);
		}else if(fp[0].equals("drawLine")){
			int x = argsAt(args,1);int y=argsAt(args,2);int x1=argsAt(args,3);int y1=argsAt(args,4);
			canvas.drawLine(x, y, x1, y1, paint);
		}else if(fp[0].equals("drawCircle")){
			paint.setAntiAlias(true);
			int x = argsAt(args,1);int y=argsAt(args,2);int r=argsAt(args,3);
			canvas.drawCircle(x, y, r, paint);
		}else if(fp[0].equals("fillCircle")){
			paint.setAntiAlias(true);
			paint.setStyle(Style.FILL);
			int x = argsAt(args,1);int y=argsAt(args,2);int r=argsAt(args,3);
			canvas.drawCircle(x, y, r, paint);
		}
	}

	private void onDrawImage(Canvas canvas, DrawConfig cfg) {
		String	src = cfg.get("src", "");
		Bitmap bitmap = BitmapCache.getInstance().getBitmap(src);
		if(bitmap != null){
			canvas.drawBitmap(bitmap, 0, 0, paint);
		}
	}

	private void onDrawText(Canvas canvas, DrawConfig cfg) {
		String	text = cfg.get("text", "");
		int	limitWidth = cfg.get("limitWidth", 1024);
		canvas.save();
		StaticLayout staticLayout = new StaticLayout(text, (TextPaint) paint, limitWidth, Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
		staticLayout.draw(canvas);
		canvas.restore();
	}

	private void onDrawLayout(Canvas canvas, DrawConfig cfg) {
		int w = config.get("width", 1);
		int h = config.get("height", 1);
		canvas.drawRect(0, 0, w, h, paint);
	}

}
