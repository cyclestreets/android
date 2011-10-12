package net.cyclestreets.api;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import java.util.Date;

import java.util.Map;
import java.util.HashMap;

import android.content.Context;

public class ApiCallCache
{
  private final Context context_;
  private final int defaultMaxAge_;
  private final Map<Integer, Long> daysToMs = new HashMap<Integer, Long>();
  private static final int DEFAULT_BUFFER_SIZE = 40960;
  
  ApiCallCache(final Context context, final int defaultMaxAge)
  {
    context_ = context;
    defaultMaxAge_ = defaultMaxAge;
  } // ApiCallCache
  
  private boolean isAvailable() 
  {
    return true;
  } // isAvailable
  
  boolean expired(final String name, int maxAgeInDays)
  {
    return expired(dataFileName(name), maxAgeInDays);
  } // expired
  
  boolean expired(final String name) 
  { 
    return expired(name, defaultMaxAge_); 
  } // expired
  
  private boolean expired(final File file, int maxAgeInDays)
  {
    final long now = new Date().getTime();
    final long fileDate = file.lastModified();
    
    final long diff = now - fileDate;
    final long expiry = expiryInMs(maxAgeInDays);
    
    return diff > expiry;
  } // expired
  
  private long expiryInMs(int maxAgeInDays)
  {
    final Long ms = daysToMs.get(maxAgeInDays);
    if(ms != null)
      return ms;
    
    final long toMs = maxAgeInDays * 24 * 60 * 60 * 1000;
    daysToMs.put(maxAgeInDays, toMs);
    return toMs;
  } // expiryInMs
  
  byte[] fetch(final String name, int maxAgeInDays)
  {
    if(!isAvailable())
      return null;
    
    final File file = dataFileName(name);
    if(!file.exists())
      return null;
    
    if(expired(file, maxAgeInDays))
      return null;
    
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    try 
    {
      final FileInputStream fis = new FileInputStream(file);
      byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    
      int n = 0;
      while ((n = fis.read(buffer)) != -1)
        output.write(buffer, 0, n);            
    } // try
    catch(IOException e)
    {
      return null;
    } // catch
    
    return output.toByteArray();    
  } // fetch
  
  byte[] fetch(final String name) 
  { 
    return fetch(name, defaultMaxAge_); 
  } // fetch
  
  void store(final String name, final byte[] value) 
  { 
    if(!isAvailable())
      return;
    
    try
    {  
      final FileOutputStream fos = new FileOutputStream(dataFileName(name));
      fos.write(value, 0, value.length);
      fos.close();
    }
    catch(IOException e)
    {
      // well, if this really does throw there's not really anything to be done
    } // catch
  } // store
  
  private File dataFileName(final String name)
  {
    final File cacheDir = context_.getCacheDir();
    return new File(cacheDir, name);
  } // dataFileName
} // class ApiCallCache
