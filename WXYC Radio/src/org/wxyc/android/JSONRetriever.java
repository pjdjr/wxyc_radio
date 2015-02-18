package org.wxyc.android;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import android.os.AsyncTask;
import android.util.Log;

/*
 * Much of the code in this class was borrowed
 * and modified to perform the required task
 * of parsing JSON coming from the WXYC playlist feed
 * See http://stackoverflow.com/questions/10164741/get-jsonarray-without-array-name
 */


public class JSONRetriever extends AsyncTask <String, Void, String>{
  //public JSONArray getPlaylist(String url){
    
  static InputStream is = null;
  static String result = "";
 
  //constructor
  public JSONRetriever(){
	  
  }
    
  protected String doInBackground(URI url) {
	    	
    HttpClient httpclient = new DefaultHttpClient();
    //URL sent to this class from MainActivity
    HttpGet httpget = new HttpGet(url);
    try {
      HttpResponse response = httpclient.execute(httpget); //creates an HTTP Get with the URL we created
                                                           //and stores the response
      HttpEntity entity = response.getEntity(); //gets the message part of the response
      is = entity.getContent(); //the getContent() method returns an InputStream object
                                //an InputStream is "a readable source of bytes (0 - 255)"
      BufferedReader reader = new BufferedReader(new InputStreamReader(is,"iso-8859-1"),8); //InputStreamReader converts the bytes in the
	                                                                 //InputStream into characters
	                                                                 //in this case of character set ISO-8859-1
	                                                                 //BufferedReader now buffers the stream--8 characters in this case
      StringBuilder sb = new StringBuilder(); //A StringBuilder is a modifiable sequence
	                                          //of characters for use in creating strings
      String line = null;
      while ((line = reader.readLine()) != null) { //readLine() returns the next
	                                         //line of text available from this reader.
      sb.append(line + "\n");
      }
      is.close(); //closes the InputStream when there's nothing else to read
      result=sb.toString(); //takes the StringBuilder object and converts it to a String
      Log.i("JSON", result);
    }
	catch (UnsupportedEncodingException e) {
    }
	catch (ClientProtocolException e) {
	} 
	catch (IOException e) {
	}
	catch (Exception e) {
        Log.e("Buffer Error", "Error converting result " + e.toString());
    }
    return result; //return type JSONArray for the getPlaylist(url) method
  }

@Override
protected String doInBackground(String... params) {
	// TODO Auto-generated method stub
	return null;
}

}
