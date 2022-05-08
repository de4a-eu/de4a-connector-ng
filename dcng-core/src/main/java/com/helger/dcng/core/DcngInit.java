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
package com.helger.dcng.core;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.security.cert.X509Certificate;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.servlet.ServletContext;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.id.factory.StringIDFromGlobalPersistentLongIDFactory;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.IURLProtocol;
import com.helger.commons.url.URLHelper;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.MessageExchangeManager;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.core.incoming.DcngIncomingHandlerViaHttp;
import com.helger.xservlet.requesttrack.RequestTrackerSettings;

import eu.de4a.kafkaclient.DE4AKafkaClient;
import eu.de4a.kafkaclient.DE4AKafkaSettings;

/**
 * Contains DE4A Connector global init and shutdown methods.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public final class DcngInit
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngInit.class);
  private static final AtomicBoolean INITED = new AtomicBoolean (false);
  private static String s_sLogPrefix;

  private DcngInit ()
  {}

  /**
   * Globally init the DE4A Connector. Calling it, if it is already initialized
   * will thrown an exception.
   *
   * @param aServletContext
   *        The servlet context used for initialization. May not be
   *        <code>null</code> but maybe a mocked one.
   * @param aIncomingHandler
   *        The incoming handler to be used. If <code>null</code> the default of
   *        {@link DcngIncomingHandlerViaHttp} will be used.
   * @throws IllegalStateException
   *         If the DE4A Connector is already initialized
   * @throws InitializationException
   *         If any of the settings are totally bogus
   */
  public static void initGlobally (@Nonnull final ServletContext aServletContext,
                                   @Nullable final IMEIncomingHandler aIncomingHandler)
  {
    if (!INITED.compareAndSet (false, true))
      throw new IllegalStateException ("DE4A Connector is already initialized");

    GlobalIDFactory.setPersistentStringIDFactory (new StringIDFromGlobalPersistentLongIDFactory ("dcng-"));
    GlobalDebug.setDebugModeDirect (DcngConfig.Global.isGlobalDebug ());
    GlobalDebug.setProductionModeDirect (DcngConfig.Global.isGlobalProduction ());

    String sLogPrefix = DcngConfig.Global.getDE4AInstanceName ();
    if (StringHelper.hasNoText (sLogPrefix))
    {
      // Get my IP address for debugging as default
      try
      {
        sLogPrefix = "[" + InetAddress.getLocalHost ().getHostAddress () + "] ";
      }
      catch (final UnknownHostException ex)
      {
        sLogPrefix = "";
      }
    }
    else
    {
      if (!sLogPrefix.startsWith ("["))
        sLogPrefix = "[" + sLogPrefix + "]";

      // Would have been trimmed when reading the properties file, so add
      // manually
      sLogPrefix += " ";
    }
    s_sLogPrefix = sLogPrefix;

    // Disable RequestTracker
    RequestTrackerSettings.setLongRunningRequestsCheckEnabled (false);
    RequestTrackerSettings.setParallelRunningRequestsCheckEnabled (false);

    {
      // Init tracker client
      final boolean bEnabled = DcngConfig.Tracker.isTrackerEnabled ();
      DE4AKafkaSettings.setKafkaEnabled (bEnabled);
      if (bEnabled)
      {
        // Use TCP or HTTP?
        final boolean bUseHTTP = DcngConfig.Tracker.isTrackerViaHttp ();
        DE4AKafkaSettings.setKafkaHttp (bUseHTTP);

        // Set tracker URL
        final String sTrackerUrl;
        if (bUseHTTP)
        {
          sTrackerUrl = DcngConfig.Tracker.getTrackerUrlHTTP ();
          if (StringHelper.hasNoText (sTrackerUrl))
            throw new InitializationException ("If the tracker is enabled, the tracker URL MUST be provided in the configuration file!");

          // Consistency check - protocol like "http://" or so must be present
          final IURLProtocol aProtocol = URLProtocolRegistry.getInstance ().getProtocol (sTrackerUrl);
          if (aProtocol == null)
            throw new InitializationException ("The tracker URL MUST start with a protocol like 'https'!");
        }
        else
        {
          sTrackerUrl = DcngConfig.Tracker.getTrackerUrlTCP ();
          if (StringHelper.hasNoText (sTrackerUrl))
            throw new InitializationException ("If the tracker is enabled, the tracker URL MUST be provided in the configuration file!");

          // Consistency check - no protocol like "http://" or so may be present
          final IURLProtocol aProtocol = URLProtocolRegistry.getInstance ().getProtocol (sTrackerUrl);
          if (aProtocol != null)
            throw new InitializationException ("The tracker URL MUST NOT start with a protocol like '" +
                                               aProtocol.getProtocol () +
                                               "'!");
        }
        DE4AKafkaSettings.defaultProperties ().put (ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, sTrackerUrl);

        // Set the topic
        final String sTrackerTopic = DcngConfig.Tracker.getTrackerTopic ();
        DE4AKafkaSettings.setKafkaTopic (sTrackerTopic);
      }
    }

    {
      // Check R2D2 configuration
      if (!DcngConfig.SMP.isUseDNS ())
      {
        final String sStaticEndpoint = DcngConfig.SMP.getStaticEndpointURL ();
        final X509Certificate aStaticCert = DcngConfig.SMP.getStaticCertificate ();
        if (URLHelper.getAsURL (sStaticEndpoint) != null && aStaticCert != null)
        {
          if (LOGGER.isInfoEnabled ())
            LOGGER.info ("Using static R2D2 target endpoint '" + sStaticEndpoint + "'");
        }
        else
        {
          final URI aSMPURI = DcngConfig.SMP.getStaticSMPUrl ();
          if (aSMPURI != null)
          {
            if (LOGGER.isInfoEnabled ())
              LOGGER.info ("Using static R2D2 SMP address '" + aSMPURI.toString () + "'");
          }
          else
            throw new InitializationException ("Since the usage of SML/DNS is disabled, the fixed URL of the SMP or the static parameters to be used must be provided in the configuration file!");
        }
      }
    }

    // Check IAL configuration
    {
      if (StringHelper.hasNoText (DcngConfig.IAL.getIALUrl ()))
        throw new InitializationException ("The IAL base URL must be configured.");
    }

    // Init incoming message handler
    final IMEIncomingHandler aRealIncomingHandler = aIncomingHandler != null ? aIncomingHandler
                                                                             : DcngIncomingHandlerViaHttp.create (s_sLogPrefix);
    MessageExchangeManager.getConfiguredImplementation ().init (aServletContext, aRealIncomingHandler);

    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> s_sLogPrefix + "DE4A Connector NG WebApp " + CDcngVersion.BUILD_VERSION + " started");
  }

  /**
   * @return <code>true</code> if the DE4A Connector was initialized (or is
   *         currently initializing or is currently shutdown),
   *         <code>false</code> if not.
   */
  public static boolean isInitialized ()
  {
    return INITED.get ();
  }

  /**
   * Globally shutdown the DE4A Connector. Calling it, if it was not already
   * initialized will thrown an exception.
   *
   * @param aServletContext
   *        The servlet context used for shutdown. May not be <code>null</code>
   *        but maybe a mocked one.
   * @throws IllegalStateException
   *         If the DE4A Connector is not initialized
   */
  public static void shutdownGlobally (@Nonnull final ServletContext aServletContext)
  {
    if (!isInitialized ())
      throw new IllegalStateException ("DE4A Connector is not initialized");

    DE4AKafkaClient.send (EErrorLevel.INFO, () -> s_sLogPrefix + "DE4A Connector shutting down");

    // Shutdown message exchange
    MessageExchangeManager.getConfiguredImplementation ().shutdown (aServletContext);

    // Shutdown tracker
    DE4AKafkaClient.close ();

    s_sLogPrefix = null;

    if (!INITED.compareAndSet (true, false))
      throw new IllegalStateException ("DE4A Connector was already shutdown");
  }
}
