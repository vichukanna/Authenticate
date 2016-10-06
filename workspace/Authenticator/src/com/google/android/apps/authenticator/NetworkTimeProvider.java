package com.google.android.apps.authenticator;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.DateParseException;
import org.apache.http.impl.cookie.DateUtils;
import java.io.IOException;
import java.util.Date;


@SuppressWarnings("deprecation")
public class NetworkTimeProvider {

  private static final String URL = "https://www.google.com";
  private String proxyAddress;
  private int proxyPort;
  
  public String getProxyAddress() {
	return proxyAddress;
}

public void setProxyAddress(String proxyAddress) {
	this.proxyAddress = proxyAddress;
}

public int getProxyPort() {
	return proxyPort;
}

public void setProxyPort(int proxyPort) {
	this.proxyPort = proxyPort;
}

public NetworkTimeProvider() { }

public NetworkTimeProvider(String proxyAddress, int proxyPort) {
	this.proxyAddress=proxyAddress;
	this.proxyPort=proxyPort;
}

  /**
   * Gets the system time by issuing a request over the network.
   *
   * @return time (milliseconds since epoch).
   *
   * @throws IOException if an I/O error occurs.
   */
  public long getNetworkTime() throws IOException {
	
	DefaultHttpClient mHttpClient = new DefaultHttpClient();
	if((this.proxyAddress!=null) && (this.proxyPort!=0)) {
		HttpHost proxy = new HttpHost(this.proxyAddress, this.proxyPort);
		mHttpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,proxy);
	}
    HttpHead request = new HttpHead(URL);
    System.out.println("Sending request to " + request.getURI());
    HttpResponse httpResponse;
    try {
      httpResponse = mHttpClient.execute(request);
    } catch (ClientProtocolException e) {
      throw new IOException(String.valueOf(e));
    } catch (IOException e) {
      throw new IOException("Failed due to connectivity issues: " + e);
    } finally {
    	mHttpClient.close();
    }

    try {
      Header dateHeader = httpResponse.getLastHeader("Date");
      System.out.println("Received response with Date header: " + dateHeader);
      if (dateHeader == null) {
        throw new IOException("No Date header in response");
      }
      String dateHeaderValue = dateHeader.getValue();
      System.out.println("dateHeaderValue " + dateHeaderValue);
      try {
        Date networkDate = DateUtils.parseDate(dateHeaderValue);
        System.out.println("networkDate " + networkDate);
        System.out.println("networkDate.getTime() " + networkDate.getTime());
        return networkDate.getTime();
      } catch (DateParseException e) {
        throw new IOException(
            "Invalid Date header format in response: \"" + dateHeaderValue + "\"");
      }
    } finally {
      // Consume all of the content of the response to facilitate HTTP 1.1 persistent connection
      // reuse and to avoid running out of connections when this methods is scheduled on different
      // threads.
      try {
        HttpEntity responseEntity = httpResponse.getEntity();
        if (responseEntity != null) {
          responseEntity.consumeContent();
        }
      } catch (IOException e) {
        // Ignored because this is not an error that is relevant to clients of this transport.
      }
    }
  }
}
