package net.aquoteeveryday;

import java.util.Date;

import net.aquoteeveryday.RestClient.RestBinder;
import net.aquoteeveryday.author.Author;
import net.aquoteeveryday.quote.Quote;
import net.aquoteeveryday.quote.QuoteController;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdView;

public class Main extends Activity {
	
	public static final String PREFS_NAME = "AQuoteEveryDay";
	public static final String PREFS_NOTIFICATION_DIALOG = "NotificationDialog";
	public static final String PREFS_NOTIFICATION = "Notification";
	public static final int NOTIFICATION_INTENT = 45;
	public static final String TAG = "AQuoteEveryDay";
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
        
        mProgressDialog = ProgressDialog.show(this, "", getString(R.string.download), true);
        
        bindRestClientService();
		createView();

		checkNotificationDialog();
    }
    
    private void createView() {
    	setContentView(R.layout.main);
    	
    	Typeface font = Typeface.createFromAsset(getAssets(), "DancingScript-Regular.ttf");
        
        mQuoteView = (TextView)findViewById(R.id.quote);
        mAuthorView = (TextView)findViewById(R.id.author);
        
        mQuoteView.setTypeface(font);
        mAuthorView.setTypeface(font);
        
        AdView adView = (AdView)this.findViewById(R.id.adView);
        adView.loadAd(new AdRequest());
	}

    @Override
    protected void onResume() {
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
		case R.id.notification:
			// Open the notification dialog
			showNotificationDialog();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
    }
    
    private void getQuote() {
    	Quote quote;
        Author author;
        QuoteController quoteController = new QuoteController(this);
        
        quote = quoteController.getQuote(new Date());
        
        if (quote == null) {
        	if (mSyncFinished) {
        		setQuoteView(getString(R.string.noQuote));
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
        
        quoteController.close();
    }
    
    private void share() {
		Quote quote;
		QuoteController quoteController = new QuoteController(this);
		
		quote = quoteController.getQuote(new Date());
        
		quoteController.close();
		
		Intent shareIntent = new Intent(android.content.Intent.ACTION_SEND);
		shareIntent.setType("text/plain");
		shareIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, quote.getText());
		shareIntent.putExtra(android.content.Intent.EXTRA_TEXT, quote.getText() + " - " + quote.getAutor().getName());
 
		startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
    }
    
    private void bindRestClientService() {
    	Intent intent = new Intent(this, RestClient.class);
    	bindService(intent, mRestClientConnection, Context.BIND_AUTO_CREATE);
    }
    
    private ServiceConnection mRestClientConnection = new ServiceConnection() {
		
		public void onServiceDisconnected(ComponentName name) {
			mBound = false;
		}
		
		public void onServiceConnected(ComponentName name, IBinder service) {
			RestBinder binder = (RestBinder) service;
			mRestClient = binder.getService();
			mBound = true;
			
			new SyncTask().execute("");
		}
	};
	
	private class SyncTask extends AsyncTask<String, String, Boolean> {

		protected Boolean doInBackground(String... parameter) {
			
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
			mSyncFinished = true;
			
			if (mProgressDialog != null) {
				mProgressDialog.dismiss();
			}
			
			if (mQuoteFound == false) {
				if (result) {
					getQuote();
				} else {
					setQuoteView(getString(R.string.noQuote));
				}
			}
		}
	}
	
	private void setQuoteView(String text) {
		mQuoteView.setText(text);
		mProgressDialog.dismiss();
	}
	
	private void createNotificationAlarm() {
		SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0); 
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean(PREFS_NOTIFICATION, true);
		editor.commit();
		
		Intent alarmSetter = new Intent(this, AlarmSetter.class);
		sendBroadcast(alarmSetter);
	}
	
	private void cancelNotificationAlarm() {
		SharedPreferences settings = getSharedPreferences(Main.PREFS_NAME, 0); 
		SharedPreferences.Editor editor = settings.edit();
		
		editor.putBoolean(PREFS_NOTIFICATION, false);
		editor.commit();
		
		Intent intent = new Intent(this, QuoteNotification.class);
		PendingIntent sender = PendingIntent.getBroadcast(this, NOTIFICATION_INTENT, intent, 0);
		AlarmManager alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
		alarm.cancel(sender);
		
		Toast.makeText(this, getString(R.string.notificationOff), Toast.LENGTH_LONG).show();
	}
	
	private void checkNotificationDialog() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0); 
		Boolean notificationDialog = settings.getBoolean(PREFS_NOTIFICATION_DIALOG, true);
		
		if (notificationDialog) {
			showNotificationDialog();
		}
	}

	private void showNotificationDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.notificationQuestion))
		       .setCancelable(false)
		       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	   setNotificationDialogPreference();
		        	   createNotificationAlarm();
		        	   dialog.cancel();
		           }
		       })
		       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
		           public void onClick(DialogInterface dialog, int id) {
		        	    setNotificationDialogPreference();
		        	    cancelNotificationAlarm();
		                dialog.cancel();
		           }
		       });
		
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private void setNotificationDialogPreference() {
		SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0); 
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(PREFS_NOTIFICATION_DIALOG, false);
		editor.commit();
	}
}