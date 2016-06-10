package net.cyclestreets.api;

import android.util.Xml;

import org.xml.sax.ContentHandler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.cyclestreets.api.json.JsonReader;
import net.cyclestreets.api.json.JsonRootHandler;

interface Factory<T> {
  T read(final byte[] xml);

  abstract class JsonProcessor<T> implements Factory<T> {
    public T read(final byte[] json) {
      try {
        doRead(json);
      } catch(IOException e) {
        throw new RuntimeException(e);
      }

      return get();
    } // read

    private void doRead(final byte[] json) throws IOException {
      final JsonReader reader = new JsonReader(byteStreamReader(json));
      try {
        rootHandler().read(reader);
      } finally {
        reader.close();
      }
    } // doRead

    protected abstract T get();
    protected abstract JsonRootHandler rootHandler();

    private static Reader byteStreamReader(final byte[] bytes) throws UnsupportedEncodingException {
      final InputStream in = new ByteArrayInputStream(bytes);
      return new InputStreamReader(in, "UTF-8");
    } // byteReader
  } // class JsonProcessor

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
