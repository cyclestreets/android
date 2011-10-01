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
} // class Factory<T>
