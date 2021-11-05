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
package com.helger.dcng.api;

import java.net.URI;
import java.security.cert.X509Certificate;

import javax.annotation.CheckForSigned;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.URLHelper;
import com.helger.config.Config;
import com.helger.config.ConfigFactory;
import com.helger.config.IConfig;
import com.helger.config.source.MultiConfigurationValueProvider;
import com.helger.peppol.sml.ESML;
import com.helger.peppol.sml.ISMLInfo;
import com.helger.peppol.sml.SMLInfo;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.security.certificate.CertificateHelper;

/**
 * This class contains global configuration elements for the DE4A Connector.
 *
 * @author Philip Helger
 */
@ThreadSafe
public final class DcngConfig
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngConfig.class);
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();

  @GuardedBy ("RW_LOCK")
  private static IConfig s_aConfig;

  static
  {
    setDefaultConfig ();
  }

  private DcngConfig ()
  {}

  /**
   * @return The configuration file. Never <code>null</code>.
   */
  @Nonnull
  public static IConfig getConfig ()
  {
    return RW_LOCK.readLockedGet ( () -> s_aConfig);
  }

  /**
   * Set a different configuration. E.g. for testing.
   *
   * @param aConfig
   *        The config to be set. May not be <code>null</code>.
   */
  public static void setConfig (@Nonnull final IConfig aConfig)
  {
    ValueEnforcer.notNull (aConfig, "Config");
    RW_LOCK.writeLocked ( () -> s_aConfig = aConfig);
  }

  /**
   * Set the default configuration.
   */
  public static void setDefaultConfig ()
  {
    final MultiConfigurationValueProvider aMCSVP = ConfigFactory.createDefaultValueProvider ();
    final IConfig aConfig = Config.create (aMCSVP);
    setConfig (aConfig);
  }

  @Nonnull
  public static IIdentifierFactory getIdentifierFactory ()
  {
    return DcngIdentifierFactory.INSTANCE;
  }

  /**
   * Global settings.
   *
   * @author Philip Helger
   */
  public static final class Global
  {
    private Global ()
    {}

    public static boolean isGlobalDebug ()
    {
      return getConfig ().getAsBoolean ("global.debug", GlobalDebug.isDebugMode ());
    }

    public static boolean isGlobalProduction ()
    {
      return getConfig ().getAsBoolean ("global.production", GlobalDebug.isProductionMode ());
    }

    @Nullable
    public static String getDE4AInstanceName ()
    {
      return getConfig ().getAsString ("global.instancename");
    }
  }

  /**
   * Global HTTP settings
   *
   * @author Philip Helger
   */
  public static final class HTTP
  {
    private HTTP ()
    {}

    public static boolean isProxyServerEnabled ()
    {
      return getConfig ().getAsBoolean ("http.proxy.enabled", false);
    }

    @Nullable
    public static String getProxyServerAddress ()
    {
      // Scheme plus hostname or IP address
      return getConfig ().getAsString ("http.proxy.address");
    }

    @CheckForSigned
    public static int getProxyServerPort ()
    {
      return getConfig ().getAsInt ("http.proxy.port", -1);
    }

    @Nullable
    public static String getProxyServerNonProxyHosts ()
    {
      // Separated by pipe
      return getConfig ().getAsString ("http.proxy.non-proxy");
    }

    public static boolean isTLSTrustAll ()
    {
      return getConfig ().getAsBoolean ("http.tls.trustall", false);
    }

    public static int getConnectionTimeoutMS ()
    {
      // -1 = system default
      return getConfig ().getAsInt ("http.connection-timeout", -1);
    }

    public static int getReadTimeoutMS ()
    {
      // -1 = system default
      return getConfig ().getAsInt ("http.read-timeout", -1);
    }
  }

  /**
   * Kafka Tracker related stuff
   *
   * @author Philip Helger
   */
  public static final class Tracker
  {
    public static final boolean DEFAULT_TRACKER_ENABLED = false;
    public static final boolean DEFAULT_TRACKER_USE_HTTP = false;
    public static final String DEFAULT_TRACKER_TOPIC = "de4a";

    private Tracker ()
    {}

    /**
     * @return <code>true</code> if the remote tracker is enabled,
     *         <code>false</code> if not.
     */
    public static boolean isTrackerEnabled ()
    {
      return getConfig ().getAsBoolean ("de4a.tracker.enabled", DEFAULT_TRACKER_ENABLED);
    }

    /**
     * @return <code>true</code> if the tracker should use http for
     *         transmission, <code>false</code> if it should use TCP. The
     *         default is TCP. When using an HTTP proxy, this should be set to
     *         true, as most HTTP proxies don't let plain TCP traffic through.
     */
    public boolean isTrackerViaHttp ()
    {
      return getConfig ().getAsBoolean ("de4a.tracker.viahttp", DEFAULT_TRACKER_USE_HTTP);
    }

    /**
     * @return The URL of the tracker. May be <code>null</code>. The layout of
     *         the URL depends whether TCP or HTTP is used.
     */
    @Nullable
    public static String getTrackerUrl ()
    {
      return getConfig ().getAsString ("de4a.tracker.url");
    }

    /**
     * @return The topic for the tracker. This should somehow reflect the
     *         Connector instance.
     */
    @Nullable
    public static String getTrackerTopic ()
    {
      return getConfig ().getAsString ("de4a.tracker.topic", DEFAULT_TRACKER_TOPIC);
    }
  }

  /**
   * SMP related settings.
   *
   * @author Philip Helger
   */
  public static final class SMP
  {
    public static final boolean DEFAULT_USE_SML = true;
    private static ISMLInfo s_aCachedSMLInfo;

    private SMP ()
    {}

    /**
     * @return <code>true</code> to use the global configuration items (see
     *         {@link DcngConfig.HTTP}) or <code>false</code> to use the custom
     *         ones from "smp client configuration". Defaults to
     *         <code>true</code>.
     */
    public static boolean isUseGlobalHttpSettings ()
    {
      return getConfig ().getAsBoolean ("de4a.smp.http.useglobalsettings", true);
    }

    /**
     * @return <code>true</code> to use SML lookup, <code>false</code> to not do
     *         it.
     * @see #getSML()
     * @see #getStaticSMPUrl()
     */
    public static boolean isUseDNS ()
    {
      return getConfig ().getAsBoolean ("de4a.smp.usedns", DEFAULT_USE_SML);
    }

    /**
     * Get a static endpoint URL to use. This method is ONLY available for BRIS
     * and effectively works around the SMP lookup by providing a constant
     * result. This value is only used if it is not empty and if the static
     * certificate is also present.<br>
     * Additionally {@link #isUseDNS()} must return <code>false</code> for this
     * method to be used.
     *
     * @return The static endpoint URL to use. May be <code>null</code>.
     * @see #getStaticCertificate()
     */
    @Nullable
    public static String getStaticEndpointURL ()
    {
      return getConfig ().getAsString ("de4a.smp.static.endpointurl");
    }

    /**
     * Get a static endpoint certificate to use. This method is ONLY available
     * for BRIS and effectively works around the SMP lookup by providing a
     * constant result. This value is only used if it is not empty and if the
     * static endpoint URL is also present.<br>
     * Additionally {@link #isUseDNS()} must return <code>false</code> for this
     * method to be used.
     *
     * @return The static endpoint URL to use. May be <code>null</code>.
     * @see #getStaticEndpointURL()
     */
    @Nullable
    public static X509Certificate getStaticCertificate ()
    {
      final String sCert = getConfig ().getAsString ("de4a.smp.static.certificate");
      if (StringHelper.hasNoText (sCert))
        return null;
      final X509Certificate ret = CertificateHelper.convertStringToCertficateOrNull (sCert);
      if (ret == null)
        LOGGER.error ("The provided static SMP certificate could NOT be parsed");
      return ret;
    }

    /**
     * @return The constant SMP URI to be used. Must only contain a value if
     *         {@link #isUseDNS()} returned <code>false</code>.
     */
    @Nullable
    public static URI getStaticSMPUrl ()
    {
      // E.g. http://smp.central.de4a
      final String sURI = getConfig ().getAsString ("de4a.smp.static.smpurl");
      return URLHelper.getAsURI (sURI);
    }

    /**
     * @return The SML URL to be used. Must only contain a value if
     *         {@link #isUseDNS()} returned <code>true</code>.
     */
    @Nonnull
    public static ISMLInfo getSML ()
    {
      ISMLInfo ret = s_aCachedSMLInfo;
      if (ret == null)
      {
        final String sSMLID = getConfig ().getAsString ("de4a.smp.sml.id");
        final ESML eSML = ESML.getFromIDOrNull (sSMLID);
        if (eSML != null)
        {
          // Pre-configured SML it is
          ret = eSML;
        }
        else
        {
          // Custom SML
          final String sDisplayName = getConfig ().getAsString ("de4a.smp.sml.name", "DE4A SML");
          // E.g. edelivery.tech.ec.europa.eu.
          final String sDNSZone = getConfig ().getAsString ("de4a.smp.sml.dnszone");
          // E.g. https://edelivery.tech.ec.europa.eu/edelivery-sml
          final String sManagementServiceURL = getConfig ().getAsString ("de4a.smp.sml.serviceurl");
          final boolean bClientCertificateRequired = getConfig ().getAsBoolean ("de4a.smp.sml.clientcert", false);
          // No need for a persistent ID here
          ret = new SMLInfo (GlobalIDFactory.getNewStringID (), sDisplayName, sDNSZone, sManagementServiceURL, bClientCertificateRequired);
        }
        // Remember in cache
        s_aCachedSMLInfo = ret;
      }
      return ret;
    }
  }

  /**
   * Global Message Exchange settings
   *
   * @author Philip Helger
   */
  public static final class ME
  {
    private ME ()
    {}

    /**
     * @return The MEM implementation ID or the default value. Never
     *         <code>null</code>.
     */
    @Nullable
    public static String getMEMImplementationID ()
    {
      return getConfig ().getAsString ("de4a.me.implementation");
    }

    /**
     * @return The DSC/DP URL where incoming AS4 messages are forwarded to. This
     *         is the value from the configuration file.
     */
    @Nullable
    public static String getMEMIncomingURL ()
    {
      return getConfig ().getAsString ("de4a.me.incoming.url");
    }
  }

  /**
   * Settings for the web application
   *
   * @author Philip Helger
   */
  public static final class WebApp
  {
    private WebApp ()
    {}

    /**
     * @return <code>true</code> if the <code>/status</code> API is enabled and
     *         may return details, <code>false</code> if not. Defaults to
     *         <code>true</code>.
     */
    public static boolean isStatusEnabled ()
    {
      return getConfig ().getAsBoolean ("de4a.webapp.status.enabled", true);
    }

    /**
     * @return The storage path for file etc. inside the Connector.
     */
    @Nullable
    public static String getDataPath ()
    {
      return getConfig ().getAsString ("de4a.webapp.data.path");
    }
  }
}
