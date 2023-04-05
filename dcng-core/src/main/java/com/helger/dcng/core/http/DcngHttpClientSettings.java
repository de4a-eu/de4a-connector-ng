package com.helger.dcng.core.http;

import java.security.GeneralSecurityException;

import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.DcngConfig;
import com.helger.httpclient.HttpClientSettings;

/**
 * Common DE4A Connector {@link HttpClientSettings} based on the configuration
 * settings.
 *
 * @author Philip Helger
 */
public class DcngHttpClientSettings extends HttpClientSettings
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngHttpClientSettings.class);

  public DcngHttpClientSettings ()
  {
    // Add settings from configuration file here centrally
    if (DcngConfig.HTTP.isProxyServerEnabled ())
    {
      setProxyHost (new HttpHost (DcngConfig.HTTP.getProxyServerAddress (), DcngConfig.HTTP.getProxyServerPort ()));

      final String sUser = DcngConfig.HTTP.getProxyUserName ();
      final String sPassword = DcngConfig.HTTP.getProxyPassword ();
      if (StringHelper.hasText (sUser))
      {
        setProxyCredentials (new UsernamePasswordCredentials (sUser,
                                                              sPassword == null ? ArrayHelper.EMPTY_CHAR_ARRAY
                                                                                : sPassword.toCharArray ()));
      }

      // Non-proxy hosts
      addNonProxyHostsFromPipeString (DcngConfig.HTTP.getProxyServerNonProxyHosts ());

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Enabled HTTP proxy settings for request");
    }

    // Disable SSL checks?
    if (DcngConfig.HTTP.isTLSTrustAll ())
      try
      {
        setSSLContextTrustAll ();
        setHostnameVerifierVerifyAll ();
        LOGGER.warn ("Trusting all TLS configurations - not recommended for production");
      }
      catch (final GeneralSecurityException ex)
      {
        throw new InitializationException ("Failed to set SSL Context or Hostname verifier", ex);
      }

    // Set timeouts
    final int nConnectTimeoutMS = DcngConfig.HTTP.getConnectTimeoutMS ();
    if (nConnectTimeoutMS >= 0)
    {
      setConnectTimeout (Timeout.ofMilliseconds (nConnectTimeoutMS));
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Using HTTP connection timeout from configuration");
    }

    final int nResponseTimeoutMS = DcngConfig.HTTP.getResponseTimeoutMS ();
    if (nResponseTimeoutMS >= 0)
    {
      setResponseTimeout (Timeout.ofMilliseconds (nResponseTimeoutMS));
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Using HTTP response timeout from configuration");
    }
  }
}
