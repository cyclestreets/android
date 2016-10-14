package net.cyclestreets.contacts;

import java.util.Comparator;

public class Contact 
{
	private final String name_;
	private final String address_;
	private final String neighbourhood_;
	private final String city_;
	private final String postcode_;
	
	public Contact(final String name, 
				   final String address,
				   final String street,
				   final String neighbourhood,
				   final String city,
				   final String postcode)
	{
		name_ = name;
		address_ = address;
		neighbourhood_ = neighbourhood;
		city_ = city;
		postcode_ = postcode;
	} // Contact
	
	public String name() { return name_; }
	public String address() { return address_; }
	public String neighbourhood() { return neighbourhood_; }
	public String city() { return city_; }
	public String postcode() { return postcode_; }
	
	public String toString() { return name_; }
	
	static public Comparator<Contact> comparator()
	{
		return ContactsComparator.instance();
	} // Comparator
	
	static private class ContactsComparator implements Comparator<Contact>
	{
		static private ContactsComparator instance_;
		static public ContactsComparator instance() 
		{ 
			if(instance_ == null)
				instance_ = new ContactsComparator();
			return instance_;
		} // instance

		@Override
		public int compare(final Contact lhs, final Contact rhs) 
		{
			return lhs.name().compareToIgnoreCase(rhs.name());
		} // compare
	} // class ContactsComparator
} // class Contact
