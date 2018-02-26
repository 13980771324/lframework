package com.ry.lframework;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.widget.FrameLayout;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 *
 * @see SystemUiHider
 */
public class FullscreenActivity extends LFramework {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        LFramework.execute("cb = sys.callback(function(e,d) print('cbnow',e,sys.toString(d));return sys.ns.app end);" +
        		"sys.ns.app:testCB(cb);" +
        		"local a = sys.inc('b/aj.lua');");
    }
	
	public void testCB(LCallBack cb) throws InterruptedException{
		System.out.println("----");
		cb.onCall("ok_im_from_java", 17894156);
	}

	public Point getPoints(){
		System.out.println("---KKK");
		return new Point(123, 4560);
	}
}
