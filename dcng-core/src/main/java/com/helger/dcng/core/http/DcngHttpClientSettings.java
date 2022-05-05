/*
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.dcng.core.http;

import java.security.GeneralSecurityException;

import org.apache.http.HttpHost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.exception.InitializationException;
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
    final int nConnectionTimeoutMS = DcngConfig.HTTP.getConnectionTimeoutMS ();
    if (nConnectionTimeoutMS >= 0)
      setConnectionTimeoutMS (nConnectionTimeoutMS);

    final int nReadTimeoutMS = DcngConfig.HTTP.getReadTimeoutMS ();
    if (nReadTimeoutMS >= 0)
      setSocketTimeoutMS (nReadTimeoutMS);
  }
}
