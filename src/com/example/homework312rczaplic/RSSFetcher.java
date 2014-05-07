package com.example.homework312rczaplic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.util.Log;

public class RSSFetcher 
{
	public static final int GOOGLE = 1;
	public static final int YAHOO = 2;
	
	private static final String GOOGLE_URL = "http://news.google.com/news/section?topic=w&output=rss";
	private static final String YAHOO_URL = "http://news.yahoo.com/rss/world";
	
	//private static final String RSS_DATE = "EEE, dd MMM yyyy HH:mm:ss zzz";
	private static final String RSS_DATE = "EEE, dd MMM yyyy HH:mm:ss";
	
	private static final String TAG = "RSSFetcher";
	
	private static final String ITEM = "item";
	private static final String CONTENT = "description";
	private static final String TITLE = "title";
	private static final String DATE = "pubDate";
	private static final String ICON = "media:content";
	
	byte[] getUrlBytes(String urlSpec) throws IOException
	{
		URL url = new URL(urlSpec);
		HttpURLConnection connection = (HttpURLConnection)url.openConnection();
		
		try
		{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			InputStream in = connection.getInputStream();
			
			if (connection.getResponseCode() != HttpURLConnection.HTTP_OK)
			{
				return null;
			}
			
			int bytesRead = 0;
			byte[] buffer = new byte[1024];
			
			while ((bytesRead = in.read(buffer)) > 0)
			{
				out.write(buffer, 0, bytesRead);				
			}
			
			out.close();
			return out.toByteArray();
		}
		finally
		{
			connection.disconnect();
		}
	}
		
	public void getUrl(ContentResolver cr, int source) throws IOException
	{
		String urlSpec;
		
		try
		{
			if (source == GOOGLE)
			{
				urlSpec = GOOGLE_URL;
			}
			else
			{
				urlSpec = YAHOO_URL;
			}
			
			String xmlString = new String(getUrlBytes(urlSpec));		
			
			XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
			XmlPullParser parser = factory.newPullParser();
			parser.setInput(new StringReader(xmlString));
			
			parseItems(cr, parser, source);
		}
		catch (IOException ioe)
		{
			Log.e(TAG, "Failed to fetch items", ioe);
		}
		catch (XmlPullParserException xppe)
		{
			Log.e(TAG, "Failed to parse items", xppe);
		}
	}
	
	void parseItems(ContentResolver cr, XmlPullParser parser, int source)
			throws XmlPullParserException, IOException
	{
		boolean insideItem = false;
		int eventType = parser.next();
		
		String title = "";
		String content = "";
		String icon = "";
		String date = "";
		long date_value = 0;
		
		while (eventType != XmlPullParser.END_DOCUMENT)
		{
			if (eventType == XmlPullParser.START_TAG &&
					ITEM.equals(parser.getName()))
			{
				insideItem = true;
				title = "";
				content = "";
				icon = "";
				date = "";
				date_value = 0;
			}
			
			if (insideItem && eventType == XmlPullParser.START_TAG &&
					TITLE.equals(parser.getName()))
			{
				title = parser.nextText();	
			}
			
			if (insideItem && eventType == XmlPullParser.START_TAG &&
					CONTENT.equals(parser.getName()))
			{
				content = parser.nextText();
			}
			
			if (insideItem && eventType == XmlPullParser.START_TAG &&
					ICON.equals(parser.getName()))
			{
				icon = parser.getAttributeValue(null, "url");
				
				Log.i(TAG, "ICON: " + icon);
			}
			
			if (insideItem && eventType == XmlPullParser.START_TAG &&
					DATE.equals(parser.getName()))
			{
				date = parser.nextText();
				
				DateFormat df = new SimpleDateFormat(RSS_DATE, Locale.ENGLISH);
				try 
				{
					Date formattedDate = df.parse(date);
					date_value = formattedDate.getTime();
				} 
				catch (ParseException e) 
				{
					e.printStackTrace();
				}				
			}
			
			if (insideItem && eventType == XmlPullParser.END_TAG && 
					ITEM.equals(parser.getName()))
			{
				// Once we hit the end of the ITEM tag, save to the database
				ContentValues cv = new ContentValues();
                cv.put(Articles.Article.TITLE, title);
                cv.put(Articles.Article.CONTENT, content);
                cv.put(Articles.Article.ICON, icon);
                cv.put(Articles.Article.DATE, date);
                cv.put(Articles.Article.DATE_VALUE, date_value);
                cv.put(Articles.Article.SOURCE, source);

                cr.insert(Articles.CONTENT_URI, cv);
                
				insideItem = false;
				//Log.i(TAG, "Saved an item");
			}
			
			eventType = parser.next();
		}
	}
}
