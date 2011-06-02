package net.cyclestreets.contacts;

import java.util.List;
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;

public class ContactsSearch 
{
	static public List<Contact> contactsList(final Context context)
	{
		return Contacts.fetch(context);
	} // contactsList	
} // class ContactsSearch
