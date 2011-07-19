package net.aquoteeveryday.author;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

public class AuthorController {

	private Context mContext;
	private AuthorDbAdapter mDb;
	
	public AuthorController(Context context) {
		mContext = context;
		
		mDb = new AuthorDbAdapter(mContext);
        mDb.open();
	}
	
	public Author getAuthor(Integer id) {
        Cursor cursor;
        Author author;
        
        cursor = mDb.getAuthorById(id);
        
        if (cursor == null) {
        	author = null;
        } else {
        	
	        String name = cursor.getString(cursor.getColumnIndexOrThrow(AuthorDbAdapter.KEY_NAME));
	        String wikipedia = cursor.getString(cursor.getColumnIndexOrThrow(AuthorDbAdapter.KEY_WIKIPEDIA));
	        
	        author = new Author(id, name, wikipedia);
        }
        
        cursor.close();
        
		return author;
		
	}
	
	public void deleteTable() {
		mDb.deleteTable();
		
		Log.i("AQuoteEveryDay", "Delete the Author table.");
	}

	public void storeAuthor(Author author) {
        Cursor cursor;
        
        cursor = mDb.getAuthorById(author.getId());
        
        if (cursor == null) {
	        Long id = mDb.storeAuthor(author);
	        
	        if (id > 0) {
	        	Log.i("AQuoteEveryDay", "The author " + author.getName() + " is stored");
	        } else {
	        	Log.i("AQuoteEveryDay", "Can't store the author " + author.getName());
	        }
        } else {
        	Log.i("AQuoteEveryDay", "The author " + author.getName() + " already exist.");
        }
	}
	
	public void close() {
		mDb.close();
	}
}
