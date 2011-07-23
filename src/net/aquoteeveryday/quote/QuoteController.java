package net.aquoteeveryday.quote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.aquoteeveryday.Main;
import net.aquoteeveryday.author.Author;
import net.aquoteeveryday.author.AuthorController;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class QuoteController {

	private Context mContext;
	
	private static QuoteDbAdapter mDb;

	public QuoteController(Context context) {
		mContext = context;
		
		mDb = new QuoteDbAdapter(mContext);
		mDb.open();
	}
	
	public Date getNewestDate() {
		Date returnValue = null;
		Cursor cursor;
        cursor = mDb.getNewestDate();
     
        if (cursor != null) {
	        String updateDate = cursor.getString(cursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_DATE));
	        
	        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
	        
	        Date date = null;
	        
			try {
				date = dateFormat.parse(updateDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
			
			returnValue = date;
			
			cursor.close();
        }
        
		return returnValue;
	}
	
	public Quote getQuote(Date date) {
		Quote returnValue = null;

		try {
        	Cursor quoteCursor;
            quoteCursor = mDb.getQuote(date);
         
            // If there is no correct quote on the db
            if (quoteCursor == null) {
            	Log.i(Main.TAG, "Can't find the Quote on the DB.");
            } else {
	            Log.i(Main.TAG, "Process the quote cursor");
	            
	            // Get the quot information from the db cursor
	            String text = quoteCursor.getString(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_TEXT));
	            Integer authorId = quoteCursor.getInt(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_AUTHORID));
	            String language = quoteCursor.getString(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_LANGUAGE));
	            
	            Author author;
	            AuthorController authorController = new AuthorController(mContext);
	            
	            Log.i(Main.TAG, "Get the quthor");
	            
	            // create the author object
	            author = authorController.getAuthor(authorId);
	            
	            authorController.close();
	            
	            try {
	            	Log.i(Main.TAG, "Create the quote object");
	            	
	            	// create the quote object
	                Quote quote = new Quote(text,
	                				  		author,
	                				  		language,
	                				  		date);
	                
	                // return the quote object
	                returnValue = quote;
				} catch (Exception e) {
					Log.e("AQuoteEveryDay", "Get Author: " + e.toString());
				}
	            
	            quoteCursor.close();
            }
		} catch (Exception e) {
			Log.e("AQuoteEveryDay", "Get Quote: " + e.toString());
		}
		
		return returnValue;
	}

	public void storeQuote(Quote quote) {
        Cursor cursor;
        
        cursor = mDb.getQuote(quote.getDate());

        if (cursor == null) {

        	try {
        		Long id = mDb.storeQuote(quote);
        		Log.i(Main.TAG,"Store Quote: " + id.toString());
			} catch (Exception e) {
				Log.e(Main.TAG, "Exeption by storing the quote on the DB: " + e.toString());
			}
	        
	        Author author = quote.getAutor();
	        
	        // If the author is not set. 
	        if (author == null) {
	        	Log.e(Main.TAG, "Author is not set");
	        } else {
	        	try {
	        		AuthorController authorController = new AuthorController(mContext);
	        		authorController.storeAuthor(author);
	        		authorController.close();
				} catch (Exception e) {
					Log.e(Main.TAG, "Exeption by storing the author: " + e.toString());
				}
	        }
        } else {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	
        	Log.i("AQuoteEveryDay", "The quote for " + dateFormat.format(quote.getDate()) + " already exist.");
        }
	}
	
	public void deleteTable() {
		mDb.deleteTable();
		
		Log.i("AQuoteEveryDay", "Delete the Quote table.");
	}
	
	public void close() {
		mDb.close();
	}
}
