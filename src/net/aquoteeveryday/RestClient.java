package net.aquoteeveryday;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.aquoteeveryday.author.Author;
import net.aquoteeveryday.quote.Quote;
import net.aquoteeveryday.quote.QuoteController;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class RestClient extends Service {
	private final IBinder mBinder = new RestBinder();

	public class RestBinder extends Binder {
		RestClient getService() {
			return RestClient.this;
		}
	}
	
	public Boolean downloadQuotes() {
		Log.i(Main.TAG, "Start downloading Quotes");
		
		Boolean returnValue = false;
		
		if (checkInternet()) {
			QuoteController quoteController = new QuoteController(getApplicationContext());
			
			try {
				Date date = quoteController.getNewestDate();

				quoteController.close();
				
				if (date == null) {
					Log.i(Main.TAG, "No newest date is set.");
				} else {
					Log.i(Main.TAG, "Newest date is: " + date.toString());
				}
					
				fetchQuote(date);
				
				returnValue = true;
			} catch (Exception e) {
				returnValue = false;
				Log.e(Main.TAG, "Extention for getNewestQuotes: " + e.toString());
			}
			
			
			Log.i(Main.TAG, "Finished downloading");
		}
		
		return returnValue;
	}
	
	private static String convertStreamToString(InputStream is) {
		/*
		 * To convert the InputStream to String we use the BufferedReader.readLine()
		 * method. We iterate until the BufferedReader return null which means
		 * there's no more data to read. Each line will appended to a StringBuilder
		 * and returned as String.
		 */
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return sb.toString();
	}
	
	private void fetchQuote(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		QuoteController quoteController = new QuoteController(getApplicationContext());
		
		String getParameter;
		
		if (date == null) {
			getParameter = "";
		} else {
			getParameter = "?date=" + dateFormat.format(date);
		}
		
		HttpClient httpclient = new DefaultHttpClient();
		HttpGet httpget = new HttpGet("http://api.aquoteeveryday.net/fetchquote.php" + getParameter);
		
		// Execute the request
		HttpResponse response;
		try {
			Log.i(Main.TAG,"Get the response from the API");
			response = httpclient.execute(httpget);
			
			// Get hold of the response entity
			HttpEntity entity = response.getEntity();
			// If the response does not enclose an entity, there is no need
			// to worry about connection release

			if (entity != null) {

				// A Simple JSON Response Read
				InputStream instream = entity.getContent();
				String result= convertStreamToString(instream);

				if (result.length() == 0) {
					Log.i(Main.TAG, "The database is up to date.");
				} else {
					Log.i(Main.TAG, "Need to store some new quotes.");
					JSONObject json = new JSONObject(result);
					JSONArray nameArray=json.names();
					JSONArray valArray=json.toJSONArray(nameArray);
					
					for(int i=0;i<valArray.length();i++)
					{
						String quoteString = valArray.getString(i);
						
						Integer authorId = 0;
						String authorName = "";
						String authorWikipedia = "";
						String text = "";
						String language = "";
						Date quoteDate = new Date();
						
						JSONObject quoteJson = new JSONObject(quoteString);
						JSONArray quoteNameArray = quoteJson.names();
						JSONArray quoteValArray = quoteJson.toJSONArray(quoteNameArray);
						
						for (int j = 0; j <quoteNameArray.length(); j++) {
							String quoteAttribute =  quoteNameArray.getString(j).trim();
							
							if (quoteAttribute.equals("authorId")) {
								authorId = quoteValArray.getInt(j);
							} else if (quoteAttribute.equals("authorName")) {
								authorName = quoteValArray.getString(j);
							} else if (quoteAttribute.equals("authorWikipedia")) {
								authorWikipedia = quoteValArray.getString(j);
							} else if (quoteAttribute.equals("text")) {
								text = quoteValArray.getString(j);  
							} else if (quoteAttribute.equals("language")) {
								language = quoteValArray.getString(j);	
							} else if (quoteAttribute.equals("date")) {
								String dateString = quoteValArray.getString(j);
								quoteDate = dateFormat.parse(dateString);
							} else {
								Log.e(Main.TAG, "Get unkown Attribute from the API >" + quoteValArray.getString(j) + "<");
							}
						}
						
						Author author = new Author(authorId, authorName, authorWikipedia);
						
						Quote quote = new Quote(text, author, language, quoteDate);
						
						try {
							quoteController.storeQuote(quote);
						} catch (Exception exception) {
							Log.e(Main.TAG, "Exeption by storing the quote: " + exception.toString());
						}
					}
				}
			}
		} catch(Exception exception) {
			Log.e(Main.TAG, "Exeption by translate the API response: " + exception.toString());
		}
		
		quoteController.close();
	}
	
	public boolean checkInternet(){
		try {
			NetworkInfo info = ((ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
			
			if (info==null || !info.isConnected()) {
                return false;
	        }
	        if (info.isRoaming()) {
	                // here is the roaming option you can change it if you want to disable internet while roaming, just return false
	                return true;
	        }
		} catch (Exception e) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(e.toString())
			       .setCancelable(false)
			       .setNeutralButton("Close", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						//dialog.cancel();
						
					}
				});
			AlertDialog alert = builder.create();
			alert.show();
			
		}
		
        return true;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}
}