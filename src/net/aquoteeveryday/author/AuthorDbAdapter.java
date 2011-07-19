/*
 * Copyright (C) 2008 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package net.aquoteeveryday.author;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class AuthorDbAdapter {

	public static final String KEY_AUTHORID = "authorId";
	public static final String KEY_NAME = "name";
    public static final String KEY_WIKIPEDIA = "wikipedia";
    public static final String KEY_ROWID = "_id";
    public static final String KEY_UPDATEDATE = "updateDate";
    public static final String KEY_INSERTDATE = "insertDate";

    private static final String DATABASE_NAME = "aquoteeveryday";
    private static final String DATABASE_TABLE = "author";
    private static final int DATABASE_VERSION = 1;
    
    private static final String TAG = "AuthorDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table if not exists " + DATABASE_TABLE + "( " + KEY_ROWID + " integer primary key AUTOINCREMENT, " +
            		KEY_AUTHORID + " integer not null, " + 
                    KEY_NAME + " varchar(250) not null, " + 
                    KEY_WIKIPEDIA + " varchar(150) not null, " + 
                    KEY_UPDATEDATE + " date not null, " + 
                    KEY_INSERTDATE + " date not null);";

    private final Context mContext;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
        	
        	Log.i("AQuoteEveryDay", "Create the author table");
            db.execSQL(DATABASE_CREATE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public AuthorDbAdapter(Context context) {
        this.mContext = context;
    }

    /**
     * Open the notes database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public AuthorDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        
        mDb.execSQL(DATABASE_CREATE);
        
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }
	
    public void dropData() {
    	mDb.delete(DATABASE_TABLE, null, null);
    }
    
    public Cursor getAuthorById(Integer id) {
    	Cursor cursor = null;
    	cursor = mDb.query(DATABASE_TABLE, new String[] {KEY_ROWID, KEY_AUTHORID, KEY_NAME, KEY_WIKIPEDIA}, KEY_AUTHORID + "=" + id.toString(), null, null, null, null);
    	
    	if (!cursor.moveToFirst()) {
    		return null;
    	}
    	
    	return cursor;
    }
	
	public long storeAuthor(Author author) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		ContentValues initialValues = new ContentValues();
		initialValues.put(KEY_AUTHORID, author.getId());
        initialValues.put(KEY_NAME, author.getName());
        initialValues.put(KEY_WIKIPEDIA, author.getWikipedia());
        initialValues.put(KEY_INSERTDATE, dateFormat.format(new Date()));
        initialValues.put(KEY_UPDATEDATE, dateFormat.format(new Date()));
        
        long returnCode = 0;
        try {
        	returnCode = mDb.insertOrThrow(DATABASE_TABLE, null, initialValues);
		} catch (Exception e) {
			Log.e("AQuoteEveryDay", e.toString());
		}
        return returnCode;
	}
	
	public void deleteTable() {
		mDb.delete(DATABASE_TABLE, null, null);
	}
}
