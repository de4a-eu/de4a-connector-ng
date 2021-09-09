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
package com.helger.rdc.api.me;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ServiceLoaderHelper;
import com.helger.rdc.api.RdcConfig;

public class MessageExchangeManager
{
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static ICommonsMap <String, IMessageExchangeSPI> s_aMap = new CommonsLinkedHashMap <> ();

  public static void reinitialize ()
  {
    RW_LOCK.writeLocked ( () -> {
      s_aMap.clear ();
      for (final IMessageExchangeSPI aImpl : ServiceLoaderHelper.getAllSPIImplementations (IMessageExchangeSPI.class))
      {
        final String sID = aImpl.getID ();
        if (s_aMap.containsKey (sID))
          throw new InitializationException ("The IMessageExchangeSPI ID '" +
                                             sID +
                                             "' is already in use - please provide a different one!");
        s_aMap.put (sID, aImpl);
      }
      if (s_aMap.isEmpty ())
        throw new InitializationException ("No IMessageExchangeSPI implementation is registered!");
    });
  }

  static
  {
    // Initial init
    reinitialize ();
  }

  private MessageExchangeManager ()
  {}

  @Nullable
  public static IMessageExchangeSPI getImplementationOfID (@Nullable final String sID)
  {
    // Fallback to default
    return RW_LOCK.readLockedGet ( () -> s_aMap.get (sID));
  }

  @Nonnull
  public static IMessageExchangeSPI getConfiguredImplementation ()
  {
    final String sID = RdcConfig.ME.getMEMImplementationID ();
    final IMessageExchangeSPI ret = getImplementationOfID (sID);
    if (ret == null)
      throw new IllegalStateException ("Failed to resolve MEM implementation with ID '" + sID + "'");
    return ret;
  }

  @Nonnegative
  public static ICommonsMap <String, IMessageExchangeSPI> getAll ()
  {
    return RW_LOCK.readLockedGet (s_aMap::getClone);
  }
}
