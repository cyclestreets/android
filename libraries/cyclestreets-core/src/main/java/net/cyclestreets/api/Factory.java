package net.cyclestreets.api;

import android.util.Xml;

import org.xml.sax.ContentHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

interface Factory<T> {
  public T read(final byte[] xml);

  abstract class XmlReader<T> implements Factory<T> {
    public T read(final byte[] xml) {
      try {
        final InputStream bais = new ByteArrayInputStream(xml);
        Xml.parse(bais,
            Xml.Encoding.UTF_8,
            contentHandler());
      } // try
      catch(final Exception e) {
        parseException(e);
      } // catch

      return get();
    } // read

    protected abstract T get();
    protected abstract ContentHandler contentHandler();

    protected void parseException(final Exception e) {
      throw new RuntimeException(e);
    } // parseException
  } // class XmlReader
} // class Factory<T>
