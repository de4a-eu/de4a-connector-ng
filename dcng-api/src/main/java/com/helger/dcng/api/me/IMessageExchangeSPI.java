package com.helger.dcng.api.me;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import com.helger.commons.annotation.IsSPIInterface;
import com.helger.commons.annotation.Nonempty;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;

/**
 * Abstract API to be implemented for sending and receiving AS4 messages. This
 * interface is used to differentiate the different AS4 implementations and
 * integrations like phase4 or Holodeck. It requires the usage of Java SPI
 * technology for registration.
 *
 * @author Philip Helger
 */
@IsSPIInterface
public interface IMessageExchangeSPI
{
  /**
   * @return The unique ID of the SPI implementation, so that it can be
   *         referenced from a configuration file. The implementer must ensure
   *         the uniqueness of the ID.
   */
  @Nonnull
  @Nonempty
  String getID ();

  /**
   * Register an incoming handler that takes the request/response to handle. The
   * differentiation between step 2/4 and 4/4 must be inside of the SPI
   * implementation. This method is only called once for the chosen
   * implementation and perform further implementation activities. If this
   * method is not called, it is ensured that
   * {@link #sendOutgoing(IMERoutingInformation, MEMessage)} of this
   * implementation are also never called.
   *
   * @param aServletContext
   *        The servlet context in which the handler should be registered. Never
   *        <code>null</code>.
   * @param aIncomingHandler
   *        The handler to use. May not be <code>null</code>.
   */
  void init (@Nonnull ServletContext aServletContext, @Nonnull IMEIncomingHandler aIncomingHandler);

  /**
   * Trigger the message transmission in step 1/4 and 3/4.
   *
   * @param aRoutingInfo
   *        Routing information. May not be <code>null</code>.
   * @param aMessage
   *        The message to be exchanged. May not be <code>null</code>.
   * @throws MEOutgoingException
   *         In case of error.
   */
  void sendOutgoing (@Nonnull IMERoutingInformation aRoutingInfo, @Nonnull MEMessage aMessage) throws MEOutgoingException;

  /**
   * Shutdown the Message Exchange.
   *
   * @param aServletContext
   *        The servlet context in which the handler should be registered. Never
   *        <code>null</code>.
   */
  void shutdown (@Nonnull ServletContext aServletContext);
}
