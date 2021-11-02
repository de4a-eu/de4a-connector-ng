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
package com.helger.dcng.holodeck.spi;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.dcng.api.me.IMessageExchangeSPI;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.dcng.holodeck.MEMDelegate;
import com.helger.dcng.holodeck.MEMDumper;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Implementation of {@link IMessageExchangeSPI} using the "TOOP AS4 Gateway
 * back-end interface" for Holodeck.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class HolodeckMessageExchangeSPI implements IMessageExchangeSPI
{
  private IMEIncomingHandler m_aIncomingHandler;

  public HolodeckMessageExchangeSPI ()
  {}

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return "holodeck";
  }

  public void init (@Nonnull final ServletContext aServletContext, @Nonnull final IMEIncomingHandler aIncomingHandler)
  {
    ValueEnforcer.notNull (aServletContext, "ServletContext");
    ValueEnforcer.notNull (aIncomingHandler, "IncomingHandler");
    if (m_aIncomingHandler != null)
      throw new IllegalStateException ("Another incoming handler was already registered!");
    m_aIncomingHandler = aIncomingHandler;

    final MEMDelegate aDelegate = MEMDelegate.getInstance ();

    aDelegate.registerNotificationHandler (aRelayResult -> {
      // more to come
      DE4AKafkaClient.send (EErrorLevel.INFO,
                            () -> "Notification[" + aRelayResult.getErrorCode () + "]: " + aRelayResult.getDescription ());
    });

    aDelegate.registerSubmissionResultHandler (aRelayResult -> {
      // more to come
      DE4AKafkaClient.send (EErrorLevel.INFO,
                            () -> "SubmissionResult[" + aRelayResult.getErrorCode () + "]: " + aRelayResult.getDescription ());
    });

    // Register the AS4 handler needed
    aDelegate.registerMessageHandler (m_aIncomingHandler::handleIncomingRequest);
  }

  public void sendOutgoing (@Nonnull final IMERoutingInformation aRoutingInfo, @Nonnull final MEMessage aMessage) throws MEOutgoingException
  {
    MEMDumper.dumpOutgoingMessage (aRoutingInfo, aMessage);
    MEMDelegate.getInstance ().sendMessage (aRoutingInfo, aMessage);
  }

  public void shutdown (@Nonnull final ServletContext aServletContext)
  {
    // empty
  }
}
