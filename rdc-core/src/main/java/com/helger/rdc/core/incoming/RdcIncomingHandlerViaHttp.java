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
package com.helger.rdc.core.incoming;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.api.me.incoming.IMEIncomingHandler;
import com.helger.rdc.api.me.incoming.MEIncomingException;
import com.helger.rdc.api.me.model.MEMessage;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Implementation of {@link IMEIncomingHandler} using
 * {@link RdcDPTriggerViaHttp} to forward the message. By default this class is
 * invoked if an incoming AS4 message is received.
 *
 * @author Philip Helger
 */
public class RdcIncomingHandlerViaHttp implements IMEIncomingHandler
{
  private final String m_sLogPrefix;

  /**
   * Constructor
   *
   * @param sLogPrefix
   *        The log prefix to use. May not be <code>null</code> but maybe empty.
   */
  protected RdcIncomingHandlerViaHttp (@Nonnull final String sLogPrefix)
  {
    m_sLogPrefix = ValueEnforcer.notNull (sLogPrefix, "LogPrefix");
  }

  public void handleIncomingRequest (@Nonnull final MEMessage aRequest) throws MEIncomingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> m_sLogPrefix + "RDC got incoming request");
    RdcDPTriggerViaHttp.forwardMessage (aRequest);
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("LogPrefix", m_sLogPrefix).getToString ();
  }

  /**
   * Factory method to create a new incoming handler
   *
   * @param sLogPrefix
   *        The log prefix to use. May not be <code>null</code> but maybe empty.
   * @return The incoming handler instance. Never <code>null</code>.
   */
  @Nonnull
  public static RdcIncomingHandlerViaHttp create (@Nonnull final String sLogPrefix)
  {
    // Check prerequisites
    if (StringHelper.hasNoText (RdcConfig.ME.getMEMIncomingURL ()))
      throw new IllegalStateException ("The MEM incoming URL for forwarding to DC/DP is not configured.");

    // go
    return new RdcIncomingHandlerViaHttp (sLogPrefix);
  }
}
