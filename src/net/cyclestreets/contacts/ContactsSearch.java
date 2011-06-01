package net.cyclestreets.contacts;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.Contacts;

public class ContactsSearch 
{
	static public List<Contact> contactsList(final Context context)
	{
		return queryAllRawContacts(context);
	} // contactsList
	
	static private List<Contact> queryAllRawContacts(final Context context) 
	{
		final List<Contact> contacts = new ArrayList<Contact>();
		
		final String[] projection = new String[] {
				RawContacts.CONTACT_ID,					// the contact id column
				RawContacts.DELETED						// column if this contact is deleted
		};
		
		final Cursor rawContacts = context.getContentResolver().query(
				RawContacts.CONTENT_URI,				// the uri for raw contact provider
				projection,	
				null,									// selection = null, retrieve all entries
				null,									// not required because selection does not contain parameters
				null);									// do not order

		try {
			final int contactIdColumnIndex = rawContacts.getColumnIndex(RawContacts.CONTACT_ID);
			final int deletedColumnIndex = rawContacts.getColumnIndex(RawContacts.DELETED);
		
			if(rawContacts.moveToFirst()) {					// move the cursor to the first entry
				while(!rawContacts.isAfterLast()) {			// still a valid entry left?
					final int contactId = rawContacts.getInt(contactIdColumnIndex);
					final boolean deleted = (rawContacts.getInt(deletedColumnIndex) == 1);
					if(!deleted) {
						final String name = displayName(context, contactId);
						final String address = address(context, contactId);
						if(name != null && address != null)
							contacts.add(new Contact(name, address));
					}
					rawContacts.moveToNext();				// move to the next entry
				}
			}
		} // try
		finally {
			rawContacts.close();
		} // finally
		
		return contacts;
	} // queryAllRawContacts
	
	private static String displayName(final Context context, final int contactId)
	{
		final String[] projection = new String[] {
				Contacts.DISPLAY_NAME
		};	

		final Cursor contact = context.getContentResolver().query(
					Contacts.CONTENT_URI,
					projection,
					Contacts._ID + "=?",						// filter entries on the basis of the contact id
					new String[]{String.valueOf(contactId)},	// the parameter to which the contact id column is compared to
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

	private static String address(final Context context, final int contactId)
	{
        final String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
							 + ContactsContract.Data.MIMETYPE + " = ?"; 
        final String[] whereParameters = new String[] { String.valueOf(contactId), 
        												ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE
        											  }; 
		final Cursor addrCur = context.getContentResolver().query(
						ContactsContract.Data.CONTENT_URI, 
						null,
						where, 
						whereParameters, 
						null); 
		
		try {
			if(addrCur.moveToFirst()) 
				return addrCur.getString(addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS));
		}
		finally {
			addrCur.close();
		} // finally
		
		return null;
	} // address
} // class ContactsSearch
