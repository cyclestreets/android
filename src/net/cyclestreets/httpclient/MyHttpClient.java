/**
 * 
 */
package net.cyclestreets.httpclient;

import net.cyclestreets.R;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;

import android.content.Context;

import java.io.InputStream;
import java.security.KeyStore;

/**
 * Based on code from http://blog.crazybob.org/2010/02/android-trusting-ssl-certificates.html
 * 
 * Works around Android not trusting newer GeoTrust certificates.
 * @author jono@nanosheep.net
 * @version Nov 15, 2010
 */

public class MyHttpClient extends DefaultHttpClient {

  final Context context;

  public MyHttpClient(Context context) {
    this.context = context;
  }

  @Override protected ClientConnectionManager createClientConnectionManager() {
    SchemeRegistry registry = new SchemeRegistry();
    registry.register(
        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
    registry.register(new Scheme("https", newSslSocketFactory(), 443));
    return new SingleClientConnManager(getParams(), registry);
  }

  /**
   * Import private keystore.
   * @return
   */
  
  private SSLSocketFactory newSslSocketFactory() {
    try {
      KeyStore trusted = KeyStore.getInstance("BKS");
      InputStream in = context.getResources().openRawResource(R.raw.mykeystore);
      try {
        trusted.load(in, "mysecret".toCharArray());
      } finally {
        in.close();
      }
      return new SSLSocketFactory(trusted);
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}

