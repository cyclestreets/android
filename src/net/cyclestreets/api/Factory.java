package net.cyclestreets.api;

import java.io.InputStream;

import org.xml.sax.ContentHandler;

import android.util.Xml;

public abstract class Factory<T>
{
  protected abstract T get();
  protected abstract ContentHandler contentHandler();
  
  protected void parseException(final Exception e)
  {
    throw new RuntimeException(e);
  } // parseException
  
  public T loadFromXml(final InputStream is)
  {
    try {
      Xml.parse(is, 
                Xml.Encoding.UTF_8, 
                contentHandler());
    } // try
    catch(final Exception e) {
      parseException(e);
    } // catch
    
    return get();
  } // loadFromXml
} // class Factory<T>
