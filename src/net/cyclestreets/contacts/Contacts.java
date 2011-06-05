package net.cyclestreets.contacts;

import java.util.List;

import android.content.Context;

public class Contacts
{
	static public List<Contact> load(final Context context)
	{
		return ContactsEclair.fetch(context);
	} // contactsList	
} // class Contacts
