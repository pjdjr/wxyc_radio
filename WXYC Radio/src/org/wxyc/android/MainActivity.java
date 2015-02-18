package org.wxyc.android;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.support.v7.app.ActionBarActivity;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity implements OnClickListener {
	
	public static final String STREAM_URL = "http://audio-mp3.ibiblio.org:8000/wxyc.mp3";
	public static final String PLAYLIST_URL = "http://www.wxyc.info/playlists/recentEntries?n="; // include value for n to determine number of entries to retrieve
	public static int onOff = 0; //onOff == 0 means media player is off. onOff == 1 means media player is on.
	MediaPlayer xycPlayer = new MediaPlayer();
	//JSON Node Names
	public static final String TAG_PLAYLIST = "playlist";
	public static final String TAG_ID = "id";
	public static final String TAG_ENTRY_TYPE = "entryType";
	public static final String TAG_HOUR = "hour";
	public static final String TAG_CHRON_ORDER_ID = "chronOrderID";
	public static final String TAG_PLAYCUT = "playcut";
	public static final String TAG_ROTATION = "rotation";
	public static final String TAG_REQUEST = "request";
	public static final String TAG_SONG_TITLE = "songTitle";
	public static final String TAG_LABEL_NAME = "labelName";
	public static final String TAG_ARTIST_NAME = "artistName";
	public static final String TAG_RELEASE_TITLE = "releaseTitle";
	private ProgressDialog pDialog;
    public static final int progress_bar_type = 0; // Progress Dialog type (0 - for Horizontal progress bar)
    private ProgressDialog prgDialog; // Progress Dialog Object
    private MediaPlayer mPlayer; // Media Player Object
	
	//playcut JSONArray
	JSONArray playcut = null;
	
	//Hashmap for ListView
	ArrayList<HashMap<String, String>> playList;
	
	private ProgressDialog pd;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

//		if (savedInstanceState == null) {
//			getSupportFragmentManager().beginTransaction()
//					.add(R.id.container, new PlaceholderFragment()).commit();
//		}
	    // Add Click listener for the button
		View theButton = findViewById(R.id.playButton);
		theButton.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	@Override
	public void onClick(View v) {
		Log.i("isPlaying?", Boolean.toString(xycPlayer.isPlaying()));
		if (xycPlayer.isPlaying()){
			Log.i("isPlaying = true", "stopping stream");
			xycPlayer.stop();
		}else{
			Log.i("isPlaying = false", "starting stream");
			new StreamMusicfromInternet().execute(STREAM_URL);
	        
	        JSONRetriever jRetriever = new JSONRetriever();//create a JSONretriever object to parse the JSON returned by the query
			URI url = null;
	        try {
				url = new URI(PLAYLIST_URL + "1");
			} catch (URISyntaxException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} //the complete URL for the playlist feed
			String result = jRetriever.doInBackground(url); //the query is sent and a String version of the JSON is returned
			JSONArray jArray = null;
			try {
				jArray = new JSONArray(result);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} //convert the String to a JSONArray
			//now we need to extract the individual fields
			HashMap playlistHashMap = extractNodes(jArray);
			setContentView(R.layout.activity_main);
	        TextView textView = (TextView) findViewById(R.id.nowPlaying);
	        textView.setText("Now Playing: \"" + playlistHashMap.get(TAG_SONG_TITLE) + "\" by " + playlistHashMap.get(TAG_ARTIST_NAME));

//	        pd.dismiss();
//	        setContentView(R.layout.activity_main);
//	        TextView textView = (TextView) findViewById(R.id.nowPlaying);
//	        textView.setText("Show current song and artist,\nrefresh every 60-120 seconds");
	        playList = new ArrayList<HashMap<String, String>>();
//?	        ListView lv = getListView();
		}	
	}
	
	private HashMap extractNodes(JSONArray jArray) {
	  //playList = new ArrayList<HashMap<String, String>>();
      //for(int i = 0; i < jArray.length(); i++){                        
		HashMap<String, String> playlistHashMap = new HashMap<String, String>();    
      JSONObject playlistEntry;
	try {
		Log.i("jArray length:", String.valueOf(jArray));
		playlistEntry = jArray.getJSONObject(0);

        //JSONObject playlistEntry = jArray.getJSONObject(i);
	    playlistEntry = jArray.getJSONObject(0);
		playlistHashMap.put(TAG_ID, playlistEntry.getString(TAG_ID));
		Log.i(TAG_ID, playlistEntry.getString(TAG_ID));
		
		playlistHashMap.put(TAG_ENTRY_TYPE, playlistEntry.getString(TAG_ENTRY_TYPE));
		Log.i(TAG_ENTRY_TYPE, playlistEntry.getString(TAG_ENTRY_TYPE));
		
		playlistHashMap.put(TAG_HOUR, playlistEntry.getString(TAG_HOUR));
		Log.i(TAG_HOUR, playlistEntry.getString(TAG_HOUR));
		playlistHashMap.put(TAG_CHRON_ORDER_ID, playlistEntry.getString(TAG_CHRON_ORDER_ID));
		if (playlistEntry.getString(TAG_ENTRY_TYPE).equals(TAG_PLAYCUT)	){
		    Log.i("Playlist entry is", TAG_PLAYCUT);
			
			JSONObject playcut = playlistEntry.getJSONObject(TAG_PLAYCUT);
			
			playlistHashMap.put(TAG_ROTATION, playcut.getString(TAG_ROTATION));
			Log.i(TAG_ROTATION, playcut.getString(TAG_ROTATION));
			
			playlistHashMap.put(TAG_REQUEST, playcut.getString(TAG_REQUEST));
			Log.i(TAG_REQUEST, playcut.getString(TAG_REQUEST));
			
			playlistHashMap.put(TAG_SONG_TITLE, capitalizeFully(playcut.getString(TAG_SONG_TITLE)));
			Log.i(TAG_SONG_TITLE, playcut.getString(TAG_SONG_TITLE));
			
			playlistHashMap.put(TAG_LABEL_NAME, capitalizeFully(playcut.getString(TAG_LABEL_NAME)));
			Log.i(TAG_LABEL_NAME, playcut.getString(TAG_LABEL_NAME));
			
			playlistHashMap.put(TAG_ARTIST_NAME, capitalizeFully(playcut.getString(TAG_ARTIST_NAME)));
			Log.i(TAG_ARTIST_NAME, playcut.getString(TAG_ARTIST_NAME));
			
			playlistHashMap.put(TAG_RELEASE_TITLE, capitalizeFully(playcut.getString(TAG_RELEASE_TITLE)));
			Log.i(TAG_RELEASE_TITLE, playcut.getString(TAG_RELEASE_TITLE));
			
		}
		//mylist.add(map);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return playlistHashMap;
	  //}
	}
		
	public static String capitalize(String str) {
	    return capitalize(str, null);
	}

	public static String capitalize(String str, char[] delimiters) {
	    int delimLen = (delimiters == null ? -1 : delimiters.length);
	    if (str == null || str.length() == 0 || delimLen == 0) {
	        return str;
	    }
	    int strLen = str.length();
	    StringBuffer buffer = new StringBuffer(strLen);
	    boolean capitalizeNext = true;
	    for (int i = 0; i < strLen; i++) {
	        char ch = str.charAt(i);

	        if (isDelimiter(ch, delimiters)) {
	            buffer.append(ch);
	            capitalizeNext = true;
	        } else if (capitalizeNext) {
	            buffer.append(Character.toTitleCase(ch));
	            capitalizeNext = false;
	        } else {
	            buffer.append(ch);
	        }
	    }
	    return buffer.toString();
	}
	private static boolean isDelimiter(char ch, char[] delimiters) {
	    if (delimiters == null) {
	        return Character.isWhitespace(ch);
	    }
	    for (int i = 0, isize = delimiters.length; i < isize; i++) {
	        if (ch == delimiters[i]) {
	            return true;
	        }
	    }
	    return false;
	}
	
    public static String capitalizeFully(String str) {
        return capitalizeFully(str, null);
    }
   
    public static String capitalizeFully(String str, char... delimiters) {
        int delimLen = (delimiters == null ? -1 : delimiters.length);
        if (str == null || str.length() == 0 || delimLen == 0) {
            return str;
        }
        str = str.toLowerCase();
        return capitalize(str, delimiters);
    }
    
    
    // Async Task Class
    class StreamMusicfromInternet extends AsyncTask<String, String, String> {

        // Show Progress bar before downloading Music
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Shows Progress Bar Dialog and then call doInBackground method
            //showDialog(progress_bar_type);
        }

        // Download Music File from Internet
        @Override
        protected String doInBackground(String... streamURL) {
            int count;
            try {
            	
            	xycPlayer.reset();
    			xycPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    			try {
    				Log.i("Stream URL = ", streamURL[0]);
    				xycPlayer.setDataSource(streamURL[0]);
    			} catch (IllegalArgumentException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IllegalStateException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			try {
    			xycPlayer.prepare();
    			} catch (IllegalStateException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
//    			pd = ProgressDialog.show(this, "Working..", "Tuning in to WXYC", true, false);
    	        xycPlayer.start();
            } catch (Exception e) {
                Log.e("Error: ", e.getMessage());
            }
            return null;
        }

        // While Downloading Music File
        protected void onProgressUpdate(String... progress) {
            // Set progress percentage
            prgDialog.setProgress(Integer.parseInt(progress[0]));
        }

        // Once Music File is downloaded
        @Override
        protected void onPostExecute(String file_url) {
            // Dismiss the dialog after the Music file was downloaded
            //dismissDialog(progress_bar_type);
        }
    }
	
}