package net.cyclestreets.api;

public class ApiCallCache
{
  final String packageName_;
  final int defaultMaxAge_;
  
  ApiCallCache(final String packageName, final int defaultMaxAge)
  {
    packageName_ = packageName;
    defaultMaxAge_ = defaultMaxAge;
  } // ApiCallCache
  
  boolean isAvailable() 
  {
    return false;
  } // isAvailable
  
  boolean expired(final String name, int maxAgeInDays)
  {
    return true;
  } // expired
  
  boolean expired(final String name) 
  { 
    return expired(name, defaultMaxAge_); 
  } // expired
  
  byte[] fetch(final String name, int maxAgeInDays)
  {
    if(!isAvailable())
      return null;
    if(expired(name, maxAgeInDays))
      return null;
    
    return null;
  } // fetch
  
  byte[] fetch(final String name) 
  { 
    return fetch(name, defaultMaxAge_); 
  } // fetch
  
  void store(final String name, final byte[] value) 
  { 
    if(!isAvailable())
      return;
  } // store
} // class ApiCallCache
