package net.cyclestreets.content;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

public class CycleStreetsContentProvider extends ContentProvider {
    private static final int PEOPLE = 1;
    private static final int PEOPLE_ID = 2;
    private static final int PEOPLE_PHONES = 3;
    private static final int PEOPLE_PHONES_ID = 4;
    private static final int PEOPLE_CONTACTMETHODS = 7;
    private static final int PEOPLE_CONTACTMETHODS_ID = 8;

    private static final int DELETED_PEOPLE = 20;

    private static final int PHONES = 9;
    private static final int PHONES_ID = 10;
    private static final int PHONES_FILTER = 14;

    private static final int CONTACTMETHODS = 18;
    private static final int CONTACTMETHODS_ID = 19;

    private static final int CALLS = 11;
    private static final int CALLS_ID = 12;
    private static final int CALLS_FILTER = 15;

    private static final UriMatcher sURIMatcher = new UriMatcher();

    static
    {
        sURIMatcher.addURI("contacts", "/people", PEOPLE);
        sURIMatcher.addURI("contacts", "/people/#", PEOPLE_ID);
        sURIMatcher.addURI("contacts", "/people/#/phones", PEOPLE_PHONES);
        sURIMatcher.addURI("contacts", "/people/#/phones/#", PEOPLE_PHONES_ID);
        sURIMatcher.addURI("contacts", "/people/#/contact_methods", PEOPLE_CONTACTMETHODS);
        sURIMatcher.addURI("contacts", "/people/#/contact_methods/#", PEOPLE_CONTACTMETHODS_ID);
        sURIMatcher.addURI("contacts", "/deleted_people", DELETED_PEOPLE);
        sURIMatcher.addURI("contacts", "/phones", PHONES);
        sURIMatcher.addURI("contacts", "/phones/filter/*", PHONES_FILTER);
        sURIMatcher.addURI("contacts", "/phones/#", PHONES_ID);
        sURIMatcher.addURI("contacts", "/contact_methods", CONTACTMETHODS);
        sURIMatcher.addURI("contacts", "/contact_methods/#", CONTACTMETHODS_ID);
        sURIMatcher.addURI("call_log", "/calls", CALLS);
        sURIMatcher.addURI("call_log", "/calls/filter/*", CALLS_FILTER);
        sURIMatcher.addURI("call_log", "/calls/#", CALLS_ID);
    }
    
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int delete(Uri arg0, String arg1, String[] arg2) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean onCreate() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		// TODO Auto-generated method stub
		return 0;
	}

}
