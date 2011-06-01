package net.cyclestreets.contacts;

public class Contact 
{
	private final String name_;
	private final String address_;
	
	public Contact(final String name, final String address)
	{
		name_ = name;
		address_ = address;
	} // Contact
	
	public String address() { return address_; }
	
	public String toString() { return name_; }
} // class Contact
