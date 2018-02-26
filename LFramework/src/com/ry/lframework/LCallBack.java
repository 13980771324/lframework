package com.ry.lframework;

public class LCallBack {
	private int luaFunction;
	public LCallBack(Object lfunc){
		if(lfunc == null)
			lfunc = 0;
		luaFunction = (Integer) lfunc;
	}
	private native void onCallImpl(int lfunc,String name,Object args);
	public void onCall(String name,Object args){
		onCallImpl(luaFunction,name,args);
	}
}
