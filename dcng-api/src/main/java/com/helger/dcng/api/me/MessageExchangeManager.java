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
package com.helger.dcng.api.me;

import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.concurrent.SimpleReadWriteLock;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.lang.ServiceLoaderHelper;
import com.helger.dcng.api.DcngConfig;

/**
 * The main class managing the {@link IMessageExchangeSPI}.
 *
 * @author Philip Helger
 */
public class MessageExchangeManager
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MessageExchangeManager.class);
  private static final SimpleReadWriteLock RW_LOCK = new SimpleReadWriteLock ();
  @GuardedBy ("RW_LOCK")
  private static final ICommonsMap <String, IMessageExchangeSPI> MAP = new CommonsLinkedHashMap <> ();

  public static void reinitialize ()
  {
    RW_LOCK.writeLocked ( () -> {
      MAP.clear ();
      for (final IMessageExchangeSPI aImpl : ServiceLoaderHelper.getAllSPIImplementations (IMessageExchangeSPI.class))
      {
        final String sID = aImpl.getID ();
        if (MAP.containsKey (sID))
          throw new InitializationException ("The IMessageExchangeSPI ID '" +
                                             sID +
                                             "' is already in use - please provide a different one!");
        MAP.put (sID, aImpl);
      }
      if (MAP.isEmpty ())
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
    return RW_LOCK.readLockedGet ( () -> MAP.get (sID));
  }

  @Nonnull
  public static IMessageExchangeSPI getConfiguredImplementation ()
  {
    final String sID = DcngConfig.ME.getMEMImplementationID ();
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Trying to find MEM implementation with ID '" + sID + "'");

    final IMessageExchangeSPI ret = getImplementationOfID (sID);
    if (ret == null)
      throw new IllegalStateException ("Failed to resolve MEM implementation with ID '" + sID + "'");
    return ret;
  }

  @Nonnegative
  public static ICommonsMap <String, IMessageExchangeSPI> getAll ()
  {
    return RW_LOCK.readLockedGet (MAP::getClone);
  }
}
