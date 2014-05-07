package com.example.homework312rczaplic;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.homework312rczaplic.Articles.Article;
import com.example.homework312rczaplic.providers.ArticlesContentProvider;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment {
	/**
	 * The fragment argument representing the item ID that this fragment
	 * represents.
	 */
	public static final String ARG_ITEM_ID = "article_id";

	public static final String TAG = "ArticleDetailFragment";
	
	private String mId;

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ArticleDetailFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (getArguments().containsKey(ARG_ITEM_ID))
		{
			mId = getArguments().getString(ARG_ITEM_ID);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		View rootView = inflater.inflate(R.layout.fragment_article_detail,
				container, false);

		// Assuming we have a valid article object, display the detail on the screen.
		if (mId != null)
		{
			Log.d(TAG, "Received id = " + mId);
			
			ContentResolver cr = getActivity().getContentResolver();
			
			Uri uri = Uri.parse("content://" + ArticlesContentProvider.AUTHORITY + "/articles/" + mId);
			
			Cursor cursor = cr.query(uri, Article.PROJECTION, null, null, null);
			
			if (cursor.getCount() > 0)
			{
				cursor.moveToFirst();
				
				if (!cursor.isAfterLast())
				{
					if (!cursor.isNull(1)) // CONTENT
					{
						((TextView) rootView.findViewById(R.id.article_detail))
							.setText(Html.fromHtml(cursor.getString(1)));
						
						((TextView) rootView.findViewById(R.id.article_detail))
							.setMovementMethod(LinkMovementMethod.getInstance());
					}
					
					if (!cursor.isNull(2)) // TITLE
					{					
						((TextView) rootView.findViewById(R.id.article_title))
							.setText(cursor.getString(2));
					}					
					
					if (!cursor.isNull(3)) // DATE
					{
						((TextView) rootView.findViewById(R.id.article_date))
							.setText(cursor.getString(3));
					}
					
					if (!cursor.isNull(4) && !cursor.getString(4).equals("")) // ICON
					{
						Log.i(TAG, "Loading URL image");
						
						// Add static Loading image first
						((ImageView) rootView.findViewById(R.id.imageView1))
							.setImageResource(R.drawable.loading);
						
						// Download the image using async task
						String URL = cursor.getString(4);
						ImageView image = (ImageView)rootView.findViewById(R.id.imageView1);
						image.setTag(URL);
						
						DownloadImageTask task = new DownloadImageTask();
						task.execute(image);
						//task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, image);
					}
					else if (!cursor.isNull(6)) // SOURCE - Google or Yahoo
					{
						if (cursor.getInt(6) == RSSFetcher.YAHOO)
						{
							Log.i(TAG, "Loading Yahoo image");
							
							((ImageView) rootView.findViewById(R.id.imageView1))
								.setImageResource(R.drawable.yahoo);
						}
						else
						{
							Log.i(TAG, "Loading Google image");
							
							((ImageView) rootView.findViewById(R.id.imageView1))
								.setImageResource(R.drawable.google);
						}
					}
					else
					{
						Log.i(TAG, "Loading no image");
					}
				}
			}
		}
		
		return rootView;
	}
}