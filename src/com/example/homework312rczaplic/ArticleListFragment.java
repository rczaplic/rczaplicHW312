package com.example.homework312rczaplic;

import java.io.IOException;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.CursorLoader;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import com.example.homework312rczaplic.ShakeDetector.OnShakeListener;


/**
 * A list fragment representing a list of Articles. This fragment also supports
 * tablet devices by allowing list items to be given an 'activated' state upon
 * selection. This helps indicate which item is currently being viewed in a
 * {@link ArticleDetailFragment}.
 * <p>
 * Activities containing this fragment MUST implement the {@link Callbacks}
 * interface.
 */
public class ArticleListFragment extends ListFragment
{
	private static final String TAG = "ArticleListFragment";
	
	private CustomAdapter mAdapter;
	
	/**
	 * The serialization (saved instance state) Bundle key representing the
	 * activated item position. Only used on tablets.
	 */
	private static final String STATE_ACTIVATED_POSITION = "activated_position";

	/**
	 * The fragment's current callback object, which is notified of list item
	 * clicks.
	 */
	private Callbacks mCallbacks = sDummyCallbacks;

	/**
	 * The current activated item position. Only used on tablets.
	 */
	private int mActivatedPosition = ListView.INVALID_POSITION;

	// Shake detector variables
	private ShakeDetector mShakeDetector;
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
	
	/**
	 * A callback interface that all activities containing this fragment must
	 * implement. This mechanism allows activities to be notified of item
	 * selections.
	 */
	public interface Callbacks {
		/**
		 * Callback for when an item has been selected.
		 */
		public void onItemSelected(String id);
	}

	/**
	 * A dummy implementation of the {@link Callbacks} interface that does
	 * nothing. Used only when this fragment is not attached to an activity.
	 */
	private static Callbacks sDummyCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(String id) {
		}
	};

	/**
	 * Mandatory empty constructor for the fragment manager to instantiate the
	 * fragment (e.g. upon screen orientation changes).
	 */
	public ArticleListFragment() {
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// ShakeDetector initialization
        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        
        // NEW IMPLEMENTATION
        mShakeDetector = new ShakeDetector();
        mShakeDetector.setOnShakeListener(new OnShakeListener()
        {
            @Override
            public void onShake(int count)
            {
				Toast.makeText(getActivity(), "Loading XML Data", Toast.LENGTH_SHORT).show();
				Log.d(TAG, "Loading XML Data");
				
				// Call async task to download information
            	new FetchItemsTask().execute();
            }
        });            
        
        CursorLoader cl = new CursorLoader(getActivity(), Articles.CONTENT_URI, 
        		Articles.Article.PROJECTION, null, null, Articles.Article.DATE_VALUE + " DESC");       
        
        Cursor c = cl.loadInBackground();
        
        if (c == null) 
        {
            Log.e(TAG, "onCreate() Null Cursor from Query");
            
        }
        
		String[] from = { Articles.Article.TITLE, Articles.Article.DATE };
		int[] to = { R.id.title_textView, R.id.date_textView };        
		
		// Create my custom cursor adapter
		mAdapter = new CustomAdapter(getActivity(), R.layout.list_item_task, c, from, to);
		  
		// Associate the simple cursor adapter to the list view
		setListAdapter(mAdapter);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		// Restore the previously serialized activated item position.
		if (savedInstanceState != null
				&& savedInstanceState.containsKey(STATE_ACTIVATED_POSITION)) {
			setActivatedPosition(savedInstanceState
					.getInt(STATE_ACTIVATED_POSITION));
		}
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Activities containing this fragment must implement its callbacks.
		if (!(activity instanceof Callbacks)) {
			throw new IllegalStateException(
					"Activity must implement fragment's callbacks.");
		}

		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onDetach() {
		super.onDetach();

		// Reset the active callbacks interface to the dummy implementation.
		mCallbacks = sDummyCallbacks;
	}

	@Override
	public void onPause() {
		mSensorManager.unregisterListener(mShakeDetector);
		super.onPause();
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(mShakeDetector, mAccelerometer, SensorManager.SENSOR_DELAY_UI);
	}	
	
	@Override
	public void onListItemClick(ListView listView, View view, int position,
			long id) {
		super.onListItemClick(listView, view, position, id);

		// Notify the active callbacks interface (the activity, if the
		// fragment is attached to one) that an item has been selected.
		
		Log.d(TAG, "TOUCHED ID = " + id);
		mCallbacks.onItemSelected(Long.toString(id));
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if (mActivatedPosition != ListView.INVALID_POSITION) {
			// Serialize and persist the activated item position.
			outState.putInt(STATE_ACTIVATED_POSITION, mActivatedPosition);
		}
	}

	/**
	 * Turns on activate-on-click mode. When this mode is on, list items will be
	 * given the 'activated' state when touched.
	 */
	public void setActivateOnItemClick(boolean activateOnItemClick) {
		// When setting CHOICE_MODE_SINGLE, ListView will automatically
		// give items the 'activated' state when touched.
		getListView().setChoiceMode(
				activateOnItemClick ? ListView.CHOICE_MODE_SINGLE
						: ListView.CHOICE_MODE_NONE);
	}

	private void setActivatedPosition(int position) {
		if (position == ListView.INVALID_POSITION) {
			getListView().setItemChecked(mActivatedPosition, false);
		} else {
			getListView().setItemChecked(position, true);
		}

		mActivatedPosition = position;
	}
	
	private class FetchItemsTask extends AsyncTask<Void,Void,Void>
	{

		@Override
		protected Void doInBackground(Void... parms)
		{
			try
			{
				// First remove all the existing data from the database
				if (getActivity() == null)
				{
					Log.i(TAG, "Preventing crash here");
					return null;
				}
				
				ContentResolver cr = getActivity().getContentResolver();
				cr.delete(Articles.CONTENT_URI, null, null);								
				
				// Then load from Google
				new RSSFetcher().getUrl(cr, RSSFetcher.GOOGLE);	

				// Then load from Yahoo
				new RSSFetcher().getUrl(cr, RSSFetcher.YAHOO);
			}
			catch (IOException ioe)
			{
				Log.e(TAG, "Failed to fetch URL: " + ioe);
			}
			return null;
		}		
	}
}
