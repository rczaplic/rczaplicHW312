package com.example.homework312rczaplic;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

public class DownloadImageTask extends AsyncTask<ImageView, Void, Bitmap> {

	private final static String TAG = "DownloadImageTask";
	
    ImageView imageView = null;
    
    @Override
    protected Bitmap doInBackground(ImageView... imageViews) {
    	Log.i(TAG, "Starting Bitmap Download");
        this.imageView = imageViews[0];
        return downloadImage((String)imageView.getTag());
    }

    @Override
    protected void onPostExecute(Bitmap result) 
    {
    	imageView.setImageBitmap(result);        
        Log.i(TAG, "Bitmap Download Complete");
    }

    private Bitmap downloadImage(String url) 
    {
        Bitmap bmp =null;
        try
        {
            URL ulrn = new URL(url);
            HttpURLConnection con = (HttpURLConnection)ulrn.openConnection();
            InputStream is = con.getInputStream();
            bmp = BitmapFactory.decodeStream(is);
            if (null != bmp)
            {
                return bmp;
            }            
        }
        catch(Exception e)
        {
        	Log.i(TAG, "Failed Bitmap Download: " + url);
        }
        return bmp;
    }
}
