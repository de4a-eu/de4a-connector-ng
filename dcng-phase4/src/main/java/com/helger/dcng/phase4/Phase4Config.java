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
package com.helger.dcng.phase4;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.config.IConfig;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.DcngConfig.WebApp;
import com.helger.phase4.crypto.AS4CryptoFactoryProperties;
import com.helger.phase4.crypto.AS4CryptoProperties;
import com.helger.phase4.crypto.IAS4CryptoFactory;
import com.helger.security.keystore.EKeyStoreType;

/**
 * Wrapper to access the configuration for the phase4 module. The configuration
 * file resolution resides in class {@link com.helger.dcng.api.DcngConfig}.
 *
 * @author Philip Helger
 */
@Immutable
public final class Phase4Config
{
  private Phase4Config ()
  {}

  @Nonnull
  private static IConfig _getConfig ()
  {
    return DcngConfig.getConfig ();
  }

  /**
   * @return The base path where phase4 should store data to. This property is
   *         only used if {@link WebApp#getDataPath()} is not used (which only
   *         has effect in dcng-webapp-*)
   */
  @Nullable
  public static String getDataPath ()
  {
    return _getConfig ().getAsString ("phase4.datapath");
  }

  /**
   * @return <code>true</code> if AS4 HTTP debugging should be enabled.
   *         Recommended to be <code>false</code>. This method is only called
   *         once on startup.
   */
  public static boolean isHttpDebugEnabled ()
  {
    return _getConfig ().getAsBoolean ("phase4.debug.http", false);
  }

  /**
   * @return <code>true</code> to debug log certain details of incoming AS4
   *         messages. This is evaluated for each incoming message.
   */
  public static boolean isDebugLogIncoming ()
  {
    return _getConfig ().getAsBoolean ("phase4.debug.incoming", false);
  }

  /**
   * @return The absolute path on disk where incoming messages should be dumped
   *         to. If the value of this property is <code>null</code> or an empty
   *         String no dumping happens. This method is only called once on
   *         startup.
   */
  @Nullable
  public static String getDumpPathIncoming ()
  {
    return _getConfig ().getAsString ("phase4.dump.incoming.path");
  }

  /**
   * @return The absolute path on disk where outgoing messages should be dumped
   *         to. If the value of this property is <code>null</code> or an empty
   *         String no dumping happens. This method is only called once on
   *         startup.
   */
  @Nullable
  public static String getDumpPathOutgoing ()
  {
    return _getConfig ().getAsString ("phase4.dump.outgoing.path");
  }

  /**
   * @return The <code>From/PartyId</code> value for receiving party id. This
   *         value must be set in the configuration and should be the CN part of
   *         the sender's X.509 AS4 certificate.
   */
  @Nullable
  public static String getFromPartyID ()
  {
    return _getConfig ().getAsString ("phase4.send.fromparty.id");
  }

  /**
   * @return The <code>From/PartyId/@type</code> for sending party id. Defaults
   *         to <code>ignore-me</code> because it must be set but we don't care.
   */
  @Nonnull
  public static String getFromPartyIDType ()
  {
    return _getConfig ().getAsString ("phase4.send.fromparty.id.type", "ignore-me");
  }

  /**
   * @return The <code>To/PartyId/@type</code> for receiving party id. Defaults
   *         to <code>ignore-me</code> because it must be set but we don't care.
   */
  @Nonnull
  public static String getToPartyIDType ()
  {
    return _getConfig ().getAsString ("phase4.send.toparty.id.type", "ignore-me");
  }

  // Key store stuff

  /**
   * @return The type of the key store, defaulting to <code>JKS</code>.
   */
  @Nonnull
  public static EKeyStoreType getKeyStoreType ()
  {
    return EKeyStoreType.getFromIDCaseInsensitiveOrDefault (_getConfig ().getAsString ("phase4.keystore.type"), EKeyStoreType.JKS);
  }

  /**
   * @return The path of the key store. May be <code>null</code>.
   */
  @Nullable
  public static String getKeyStorePath ()
  {
    return _getConfig ().getAsString ("phase4.keystore.path");
  }

  /**
   * @return The password of the key store. May be <code>null</code>.
   */
  @Nullable
  public static String getKeyStorePassword ()
  {
    return _getConfig ().getAsString ("phase4.keystore.password");
  }

  /**
   * @return The alias of the key inside the key store. May be
   *         <code>null</code>.
   */
  @Nullable
  public static String getKeyStoreKeyAlias ()
  {
    return _getConfig ().getAsString ("phase4.keystore.key-alias");
  }

  /**
   * @return The password of the alias inside the key store. May be
   *         <code>null</code>.
   */
  @Nullable
  public static String getKeyStoreKeyPassword ()
  {
    return _getConfig ().getAsString ("phase4.keystore.key-password");
  }

  // Truststore stuff

  /**
   * @return The type of the trust store, defaulting to <code>JKS</code>.
   */
  @Nonnull
  public static EKeyStoreType getTrustStoreType ()
  {
    return EKeyStoreType.getFromIDCaseInsensitiveOrDefault (_getConfig ().getAsString ("phase4.truststore.type"), EKeyStoreType.JKS);
  }

  /**
   * @return The path of the trust store. May be <code>null</code>.
   */
  @Nullable
  public static String getTrustStorePath ()
  {
    return _getConfig ().getAsString ("phase4.truststore.path");
  }

  /**
   * @return The password of the trust store. May be <code>null</code>.
   */
  @Nullable
  public static String getTrustStorePassword ()
  {
    return _getConfig ().getAsString ("phase4.truststore.password");
  }

  @Nonnull
  public static IAS4CryptoFactory getCryptoFactory ()
  {
    return new AS4CryptoFactoryProperties (new AS4CryptoProperties ().setKeyStoreType (getKeyStoreType ())
                                                                     .setKeyStorePath (getKeyStorePath ())
                                                                     .setKeyStorePassword (getKeyStorePassword ())
                                                                     .setKeyAlias (getKeyStoreKeyAlias ())
                                                                     .setKeyPassword (getKeyStoreKeyPassword ())
                                                                     .setTrustStoreType (getTrustStoreType ())
                                                                     .setTrustStorePath (getTrustStorePath ())
                                                                     .setTrustStorePassword (getTrustStorePassword ()));
  }
}
