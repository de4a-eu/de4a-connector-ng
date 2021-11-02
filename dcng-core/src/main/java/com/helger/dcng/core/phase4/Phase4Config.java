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
package com.helger.dcng.core.phase4;

import javax.annotation.Nonnull;

import com.helger.dcng.api.DcngConfig;
import com.helger.phase4.crypto.AS4CryptoFactoryProperties;
import com.helger.phase4.crypto.AS4CryptoProperties;
import com.helger.phase4.crypto.IAS4CryptoFactory;

/**
 * Wrapper to access the configuration for the phase4 module. The configuration
 * file resolution resides in class {@link com.helger.dcng.api.DcngConfig.Phase4}.
 *
 * @author Philip Helger
 */
public final class Phase4Config
{
  private Phase4Config ()
  {}

  @Nonnull
  public static IAS4CryptoFactory getCryptoFactory ()
  {
    return new AS4CryptoFactoryProperties (new AS4CryptoProperties ().setKeyStoreType (DcngConfig.Phase4.getKeyStoreType ())
                                                                     .setKeyStorePath (DcngConfig.Phase4.getKeyStorePath ())
                                                                     .setKeyStorePassword (DcngConfig.Phase4.getKeyStorePassword ())
                                                                     .setKeyAlias (DcngConfig.Phase4.getKeyStoreKeyAlias ())
                                                                     .setKeyPassword (DcngConfig.Phase4.getKeyStoreKeyPassword ())
                                                                     .setTrustStoreType (DcngConfig.Phase4.getTrustStoreType ())
                                                                     .setTrustStorePath (DcngConfig.Phase4.getTrustStorePath ())
                                                                     .setTrustStorePassword (DcngConfig.Phase4.getTrustStorePassword ()));
  }
}
