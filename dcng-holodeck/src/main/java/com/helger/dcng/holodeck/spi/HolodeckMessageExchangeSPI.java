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
