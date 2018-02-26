package com.ry.lframework.ui;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;

import com.ry.lframework.LFramework;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class BitmapCache {
	private static BitmapCache instance;
	private HashMap<String, Bitmap> caches;
	private BitmapCache(){
		caches = new HashMap<String,Bitmap>();
	}
	
	public static BitmapCache getInstance(){
		if(instance == null)
			instance = new BitmapCache();
		return instance;
	}
	
	public void add(String path,Bitmap value){
		if(path == null)
			return;
		caches.put(path, value);
	}

	private Bitmap getImageFromAssets(String path){
		Bitmap image = null;
		AssetManager assets = LFramework.getInstance().getResources().getAssets();
		try{
			InputStream is = assets.open(path);
			image = BitmapFactory.decodeStream(is);
			is.close();
		}catch (IOException e){
			e.printStackTrace();
		}
		return image;
	}
	private Bitmap getImageFromNetwork(String path) {
		return null;
	}
	public Bitmap getBitmap(String path){
		if(path == null)
			return null;
		Bitmap result = caches.get(path);
		if(result == null){
			if(path.startsWith("http://") || path.startsWith("https://")){
				result = getImageFromNetwork(path);
			}else{
				result = getImageFromAssets(path);
			}
			add(path,result);
		}
		return result;
	}
	public void remove(String path){
		Bitmap bitmap = caches.remove(path);
		if(bitmap != null){
			bitmap.recycle();
		}
	}
	public void removeAll(){
		Iterator<String> keys = caches.keySet().iterator();
		while(keys.hasNext()){
			Bitmap bitmap = caches.get(keys.next());
			bitmap.recycle();
		}
		caches.clear();
	}
	public void dump(){
		System.out.println("----------------------------------------");
		Iterator<String> keys = caches.keySet().iterator();
		while(keys.hasNext()){
			String key = keys.next();
			Bitmap bitmap = caches.get(key);
			int bytes =  bitmap.getRowBytes() * bitmap.getHeight();
			System.out.println(">>" + key + "|" + bitmap.getWidth() + "x" + bitmap.getHeight() + "@" + (bytes/1024) + "kb");
		}
	}
}
