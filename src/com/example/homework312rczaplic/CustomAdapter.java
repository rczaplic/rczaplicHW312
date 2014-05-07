package com.example.homework312rczaplic;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends SimpleCursorAdapter 
{
	//private final static String TAG = "CustomAdapter";
	
	Cursor items;
	private int layout;
	
	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) 
	{
	    final LayoutInflater inflater = LayoutInflater.from(context);
	    View v = inflater.inflate(layout, parent, false);
	    
	    return v;
	}
	
	
	@SuppressWarnings("deprecation")
	public CustomAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
	    super(context, layout, c, from, to);
	    this.layout = layout;
	}
	
	
	@Override
	public void bindView(View v, Context context, Cursor c) 
	{	
		String title = "";
		String date = "";
		String icon = "";
		
	    int titleCol = c.getColumnIndex("title");
	    title = c.getString(titleCol);
	    
	    int dateCol = c.getColumnIndex("date");
	    date = c.getString(dateCol);

	    int iconCol = c.getColumnIndex("icon");
	    icon = c.getString(iconCol);
	    
	    int sourceCol = c.getColumnIndex("source");
	    long source = c.getLong(sourceCol);	    	    
	    
	    // Display Title info
	    TextView title_text = (TextView) v.findViewById(R.id.title_textView);
	    if (title_text != null) {
	    	title_text.setText(title);
	    }
	    
	    // Display Data info
	    TextView date_text = (TextView) v.findViewById(R.id.date_textView);
	    if (date_text != null) {
	    	date_text.setText(date);
	    }
	
	    // Display Image graphic
	    ImageView image = (ImageView) v.findViewById(R.id.icon_imageView);
	    
	    if (icon.isEmpty())
	    {		    
		    if (image != null)
		    {
		    	if (source == RSSFetcher.GOOGLE)
		    	{
		    		image.setImageResource(R.drawable.google);
		    	}
		    	else
		    	{
		    		image.setImageResource(R.drawable.yahoo);
		    	}
		    }
	    }
	    else
	    {
	    	image.setImageResource(R.drawable.loading);
	    	
	    	if (image != null)
	    	{
		    	image.setTag(icon);
				
				DownloadImageTask task = new DownloadImageTask();
				task.execute(image);
				//task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
	    	}
	    }
	}
}
