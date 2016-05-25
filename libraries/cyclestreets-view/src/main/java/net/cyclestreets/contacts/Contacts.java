package net.cyclestreets.contacts;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

public class Contacts
{
	static public List<Contact> load(final Context context) 
	{
		final List<Contact> contacts = new ArrayList<>();
		
		final String[] projection = new String[] {
				ContactsContract.Data.CONTACT_ID,
				ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS,
				ContactsContract.CommonDataKinds.StructuredPostal.STREET,
				ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD,
				ContactsContract.CommonDataKinds.StructuredPostal.CITY,
				ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE
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
			final int streetIndex = addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET);
			final int neighbourhoodIndex = addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD);
			final int cityIndex = addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY);
			final int postcodeIndex = addrCur.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE);
			
		
			if(addrCur.moveToFirst()) {					// move the cursor to the first entry
				while(!addrCur.isAfterLast()) {			// still a valid entry left?
					final String id = addrCur.getString(idIndex);
					final String address = addrCur.getString(addressIndex);
					final String street = addrCur.getString(streetIndex);
					final String neighbourhood = addrCur.getString(neighbourhoodIndex);
					final String city = addrCur.getString(cityIndex);
					final String postcode = addrCur.getString(postcodeIndex);

					final String name = displayName(context, id);

					if(name != null && address != null)
						contacts.add(new Contact(name,
												 address,
												 street,
												 neighbourhood,
												 city,
												 postcode));

					addrCur.moveToNext();				// move to the next entry
				} // while ...
			} // if ...
		} // try
		finally {
			addrCur.close();
		} // finally
		
		Collections.sort(contacts, Contact.comparator());
		
		return contacts;
	} // queryContacts
	
	private static String displayName(final Context context, final String contactId)
	{
		final String[] projection = new String[] {
		    ContactsContract.Contacts.DISPLAY_NAME
		};	

		final Cursor contact = context.getContentResolver().query(
		      ContactsContract.Contacts.CONTENT_URI,
					projection,
					ContactsContract.Contacts._ID + "=?",		
					new String[] { contactId },
					null);
		try {
			if(contact.moveToFirst()) 
				return contact.getString(contact.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		}
		finally {
			contact.close();
		} // finally
		
		return null;
	} // displayName
} // class Contacts
