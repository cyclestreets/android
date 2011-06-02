package net.cyclestreets.contacts;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.ContactsContract;

public class Contacts 
{
	static public List<Contact> fetch(final Context context) 
	{
		final List<Contact> contacts = new ArrayList<Contact>();
		
		final String[] projection = new String[] {
				ContactsContract.Data.CONTACT_ID,
				ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS
		};

        final String where = ContactsContract.Data.MIMETYPE + " = ?"; 
        final String[] whereParameters = new String[] { ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE }; 
		
		final Cursor addrCur = context.getContentResolver().query(
				ContactsContract.Data.CONTENT_URI, 
				projection,
				where, 
				whereParameters, 
				null); 

		try {
			final int idIndex = addrCur.getColumnIndex(ContactsContract.Data.CONTACT_ID);
			final int addressIndex = addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS);
		
			if(addrCur.moveToFirst()) {					// move the cursor to the first entry
				while(!addrCur.isAfterLast()) {			// still a valid entry left?
					final String id = addrCur.getString(idIndex);
					final String address = addrCur.getString(addressIndex);

					final String name = displayName(context, id);

					if(name != null && address != null)
						contacts.add(new Contact(name, address));

					addrCur.moveToNext();				// move to the next entry
				} // while ...
			} // if ...
		} // try
		finally {
			addrCur.close();
		} // finally
		
		return contacts;
	} // queryContacts
	
	private static String displayName(final Context context, final String contactId)
	{
		final String[] projection = new String[] {
				Contacts.DISPLAY_NAME
		};	

		final Cursor contact = context.getContentResolver().query(
					Contacts.CONTENT_URI,
					projection,
					Contacts._ID + "=?",		
					new String[] { contactId },
					null);
		try {
			if(contact.moveToFirst()) 
				return contact.getString(contact.getColumnIndex(Contacts.DISPLAY_NAME));
		}
		finally {
			contact.close();
		} // finally
		
		return null;
	} // displayName
} // class Contacts
