/**
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
package com.helger.rdc.api.http;

import java.security.GeneralSecurityException;

import org.apache.http.HttpHost;

import com.helger.commons.exception.InitializationException;
import com.helger.httpclient.HttpClientSettings;
import com.helger.rdc.api.TCConfig;

/**
 * Common DE4A Connector {@link HttpClientSettings} based on the configuration
 * settings.
 *
 * @author Philip Helger
 */
public class TCHttpClientSettings extends HttpClientSettings
{
  public TCHttpClientSettings ()
  {
    // Add settings from configuration file here centrally
    if (TCConfig.HTTP.isProxyServerEnabled ())
    {
      setProxyHost (new HttpHost (TCConfig.HTTP.getProxyServerAddress (), TCConfig.HTTP.getProxyServerPort ()));

      // Non-proxy hosts
      addNonProxyHostsFromPipeString (TCConfig.HTTP.getProxyServerNonProxyHosts ());
    }

    // Disable SSL checks?
    if (TCConfig.HTTP.isTLSTrustAll ())
      try
      {
        setSSLContextTrustAll ();
        setHostnameVerifierVerifyAll ();
      }
      catch (final GeneralSecurityException ex)
      {
        throw new InitializationException (ex);
      }

    final int nConnectionTimeoutMS = TCConfig.HTTP.getConnectionTimeoutMS ();
    if (nConnectionTimeoutMS >= 0)
      setConnectionTimeoutMS (nConnectionTimeoutMS);

    final int nReadTimeoutMS = TCConfig.HTTP.getReadTimeoutMS ();
    if (nReadTimeoutMS >= 0)
      setSocketTimeoutMS (nReadTimeoutMS);
  }
}
