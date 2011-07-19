package net.aquoteeveryday;

import java.util.Date;

import net.aquoteeveryday.RestClient.RestBinder;
import net.aquoteeveryday.author.Author;
import net.aquoteeveryday.quote.Quote;
import net.aquoteeveryday.quote.QuoteController;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

public class Main extends Activity {
	
	public static String TAG = "AQuoteEveryDay";
	private static String NO_QUOTE = "No Quote :(";
	private TextView mQuoteView;
	private TextView mAuthorView;
	private Boolean mQuoteFound = false;
	private Boolean mSyncFinished = false;
	private RestClient mRestClient;
	private Boolean mBound = false;
	private ProgressDialog mProgressDialog;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i("AQuoteEveryDay", "Create Main");
        
        mProgressDialog = ProgressDialog.show(this, "", "Loading. Please wait...", true);
        
        bindRestClientService();
		createView();
		getQuote();
    }
    
    private void createView() {
    	Log.i(Main.TAG, "Create View");
    	setContentView(R.layout.main);
    	
    	Typeface font = Typeface.createFromAsset(getAssets(), "DancingScript-Regular.ttf");
        
        mQuoteView = (TextView)findViewById(R.id.quote);
        mAuthorView = (TextView)findViewById(R.id.author);
        
        mQuoteView.setTypeface(font);
        mAuthorView.setTypeface(font);
	}

    @Override
    protected void onResume() {
    	Log.i(Main.TAG, "onResume");
    	super.onResume();
    	
    	getQuote();
    }
    
    @Override
    protected void onStop() {
    	super.onStop();
    	if (mBound) {
			unbindService(mRestClientConnection);
			mBound = false;
		}
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
    	MenuInflater inflater = getMenuInflater();
    	
    	inflater.inflate(R.menu.main, menu);
    	
		return true;
    	
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch (item.getItemId()) {
		case R.id.share:
			// Open the share menu
			share();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
    }
    
    private void getQuote() {
    	Log.i(Main.TAG, "Get Quote");
    	
    	Quote quote;
        Author author;
        QuoteController quoteController = new QuoteController(this);
        
        quote = quoteController.getQuote(new Date());
        
        if (quote == null) {
        	if (mSyncFinished) {
        		setQuoteView(NO_QUOTE);
        	}
        } else {
        	mQuoteFound = true;
        	
        	setQuoteView(quote.getText());
        	
        	author = quote.getAutor();
        
        	if (author == null ) {
        		mAuthorView.setText("Unknown");
        	} else {
        		mAuthorView.setText(author.getName());
        	}
        }
        
        Log.i(Main.TAG, "Get Quote Finish");
    }
    
    private void share() {
		Quote quote;
		QuoteController quoteController = new QuoteController(this);
		
		quote = quoteController.getQuote(new Date());
        
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, quote.getText());
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, quote.getText() + " - " + quote.getAutor().getName());
 
		startActivity(Intent.createChooser(shareIntent, "Share this Quote"));
    }
    
    private void bindRestClientService() {
    	Intent intent = new Intent(this, RestClient.class);
    	Log.i(Main.TAG, "Bind the RestClient Service");
    	bindService(intent, mRestClientConnection, Context.BIND_AUTO_CREATE);
    }
    
    private ServiceConnection mRestClientConnection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
			Log.i(Main.TAG, "RestClient disconnected");
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			RestBinder binder = (RestBinder) service;
			mRestClient = binder.getService();
			mBound = true;
			Log.i(Main.TAG, "RestClient connected");
			
			new SyncTask().execute("");
		}
	};
	
	private class SyncTask extends AsyncTask<String, String, Boolean> {

		protected Boolean doInBackground(String... parameter) {
			Log.i(Main.TAG,"Start Sync");
			
			Boolean returnValue = false;
			
			if (mBound) {
				if (mRestClient.checkInternet()) {
					mRestClient.downloadQuotes();
					returnValue = true;
				} else {
					Log.e(Main.TAG, "No Internet Connection");
				}
			} else {
				Log.e(Main.TAG, "The RestClient Service is not binded.");
			}
			
			return returnValue;
		}

		protected void onPostExecute(Boolean result) {
			Log.i(Main.TAG, "Post Execute with result: " + result.toString());
			
			mSyncFinished = true;
			
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			
			if (mQuoteFound == false) {
				if (result) {
					getQuote();
				} else {
					setQuoteView(NO_QUOTE);
				}
			}
		}
	}
	
	private void setQuoteView(String text) {
		mQuoteView.setText(text);
		mProgressDialog.dismiss();
	}
}