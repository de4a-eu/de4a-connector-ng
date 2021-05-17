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
package com.helger.rdc.core;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.rdc.api.me.incoming.IMEIncomingHandler;
import com.helger.rdc.api.me.incoming.IncomingEDMRequest;
import com.helger.rdc.api.me.incoming.IncomingEDMResponse;
import com.helger.rdc.api.me.incoming.MEIncomingException;
import com.helger.rdc.core.incoming.DC_DP_TriggerViaHttp;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Implementation of {@link IMEIncomingHandler} using
 * {@link DC_DP_TriggerViaHttp} to forward the message. By default this class is
 * invoked if an incoming AS4 message is received.
 *
 * @author Philip Helger
 */
public class TCIncomingHandlerViaHttp implements IMEIncomingHandler
{
  private final String m_sLogPrefix;

  /**
   * @param sLogPrefix
   *        The log prefix to use. May not be <code>null</code> but maybe empty.
   */
  public TCIncomingHandlerViaHttp (@Nonnull final String sLogPrefix)
  {
    m_sLogPrefix = ValueEnforcer.notNull (sLogPrefix, "LogPrefix");
  }

  public void handleIncomingRequest (@Nonnull final IncomingEDMRequest aRequest) throws MEIncomingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> m_sLogPrefix + "TC got DP incoming MEM request (2/4)");
    DC_DP_TriggerViaHttp.forwardMessage (aRequest);
  }

  public void handleIncomingResponse (@Nonnull final IncomingEDMResponse aResponse) throws MEIncomingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> m_sLogPrefix +
                                "TC got DC incoming MEM response (4/4) with " +
                                aResponse.attachments ().size () +
                                " attachments");
    DC_DP_TriggerViaHttp.forwardMessage (aResponse);
  }
}
