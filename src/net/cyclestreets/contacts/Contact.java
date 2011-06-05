package net.cyclestreets.contacts;

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
	
	public String address() { return address_; }
	public String neighbourhood() { return neighbourhood_; }
	public String city() { return city_; }
	public String postcode() { return postcode_; }
	
	public String toString() { return name_; }
} // class Contact
