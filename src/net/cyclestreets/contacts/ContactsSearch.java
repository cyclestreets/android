package net.cyclestreets.contacts;

import java.util.List;

import android.content.Context;

public class ContactsSearch 
{
	static public List<Contact> contactsList(final Context context)
	{
		return ContactsEclair.fetch(context);
	} // contactsList	
} // class ContactsSearch
