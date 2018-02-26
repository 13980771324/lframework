package com.ry.lframework.ui;

import java.util.ArrayList;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ListAdapter;
import android.widget.ListView;

public class List extends DisplayObejct implements ListAdapter{
	private ListView listView;
	private ArrayList<DisplayObejct> items;

	public List(Context c,String url) {
		super(c, ViewLayer.DrawTypeVIEW, url, new ListView(c));
		items = new ArrayList<DisplayObejct>();
		listView = bindLayer.getConfig().get("view", null);
		listView.setLayoutParams(new LayoutParams(getWidth(), getHeight()));
		listView.setAdapter(this);
	}
	
	public void push(DisplayObejct value){
		items.add(value);
	}

	public void remove(DisplayObejct value){
		items.remove(value);
	}

	@Override
	public int getCount() {
		return items.size();
	}

	@Override
	public View getView(int position, View current, ViewGroup parent) {
		DisplayObejct v = items.get(position);
		current = v.bindLayer;
		if(current != null){
			v.removeFromParent();
		}
		return current;
	}

	@Override
	public Object getItem(int position) {
		return items.get(position).bindLayer;
	}

	@Override
	public long getItemId(int position) {
		return items.get(position).bindLayer.getId();
	}

	@Override
	public int getItemViewType(int position) {
		return 1;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isEmpty() {
		return false;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		
	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int arg0) {
		return true;
	}

}
