package net.cyclestreets.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "cyclestreets.db";
    private static final int DATABASE_VERSION = 1;
    //private static final String JOURNEY_TABLE_NAME = "journey";
	
    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
	@Override
	public void onCreate(SQLiteDatabase db) {
//        db.execSQL("CREATE TABLE " + NOTES_TABLE_NAME + " ("
//                + Notes._ID + " INTEGER PRIMARY KEY,"
//                + Notes.TITLE + " TEXT,"
//                + Notes.NOTE + " TEXT,"
//                + Notes.CREATED_DATE + " INTEGER,"
//                + Notes.MODIFIED_DATE + " INTEGER"
//                + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// TODO Auto-generated method stub

	}

}
