package net.aquoteeveryday.quote;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

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
        Cursor cursor;
        cursor = mDb.getNewestDate();
     
        if (cursor == null) {
        	return null;
        }
        
        String updateDate = cursor.getString(cursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_DATE));
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        
        Date date = null;
        
		try {
			date = dateFormat.parse(updateDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
		return date;
	}
	
	public Quote getQuote(Date date) {
        try {
        	Cursor quoteCursor;
            quoteCursor = mDb.getQuote(date);
         
            if (quoteCursor == null) {
            	return null;
            }
            
            String text = quoteCursor.getString(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_TEXT));
            Integer authorId = quoteCursor.getInt(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_AUTHORID));
            String language = quoteCursor.getString(quoteCursor.getColumnIndexOrThrow(QuoteDbAdapter.KEY_LANGUAGE));
            
            Author author;
            AuthorController authorController = new AuthorController(mContext);
            
            author = authorController.getAuthor(authorId);
            
            try {
                Quote quote = new Quote(text,
                				  		author,
                				  		language,
                				  		date);
                
                return quote;
			} catch (Exception e) {
				Log.e("AQuoteEveryDay", "Get Author: " + e.toString());
				return null;
			}
            
            
    		
		} catch (Exception e) {
			Log.e("AQuoteEveryDay", "Get Quote: " + e.toString());
			return null;
		}
	}

	public void storeQuote(Quote quote) {
        Cursor cursor;
        
        cursor = mDb.getQuote(quote.getDate());
        
        if (cursor == null) {
	        Long id = mDb.storeQuote(quote);
	        
	        if (id > 0) {
	        	Log.i("AQuoteEveryDay", "The quote " + id.toString() + " is stored");
	        } else {
	        	Log.i("AQuoteEveryDay", "Can't store the quote");
	        }
	        
	        Author author = quote.getAutor();
	        AuthorController authorController = new AuthorController(mContext);
	        
	        authorController.storeAuthor(author);
	        
        } else {
        	SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        	
        	Log.i("AQuoteEveryDay", "The quote for " + dateFormat.format(quote.getDate()) + " already exist.");
        }
        
	}
	
	public void deleteTable() {
		mDb.deleteTable();
		
		Log.i("AQuoteEveryDay", "Delete the Quote table.");
	}
}
