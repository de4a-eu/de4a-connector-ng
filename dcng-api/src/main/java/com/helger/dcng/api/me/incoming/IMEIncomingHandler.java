package com.helger.dcng.api.me.incoming;

import javax.annotation.Nonnull;

import com.helger.dcng.api.me.model.MEMessage;

/**
 * The callback handler for incoming messages from the AS4 Gateway. An
 * implementation of this interface must be provided when calling
 * "DcngInit.initGlobally". The default implementation is
 * "DcngIncomingHandlerViaHttp". If you are embedding the DCNG into your
 * application you must provide an implementation of this interface.
 *
 * @author Philip Helger
 */
public interface IMEIncomingHandler
{
  /**
   * Handle an incoming request (for DC or DP).
   *
   * @param aMessage
   *        The message to handle. Never <code>null</code>.
   * @throws MEIncomingException
   *         In case of error.
   */
  void handleIncomingRequest (@Nonnull MEMessage aMessage) throws MEIncomingException;
}
