package com.helger.dcng.core.incoming;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.api.me.incoming.MEIncomingException;
import com.helger.dcng.api.me.model.MEMessage;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Implementation of {@link IMEIncomingHandler} using
 * {@link DcngDPTriggerViaHttp} to forward the message. By default this class is
 * invoked if an incoming AS4 message is received.
 *
 * @author Philip Helger
 */
public class DcngIncomingHandlerViaHttp implements IMEIncomingHandler
{
  private final String m_sLogPrefix;

  /**
   * Constructor
   *
   * @param sLogPrefix
   *        The log prefix to use. May not be <code>null</code> but maybe empty.
   */
  protected DcngIncomingHandlerViaHttp (@Nonnull final String sLogPrefix)
  {
    m_sLogPrefix = ValueEnforcer.notNull (sLogPrefix, "LogPrefix");
  }

  public void handleIncomingRequest (@Nonnull final MEMessage aRequest) throws MEIncomingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> m_sLogPrefix + "DCNG got incoming request");
    DcngDPTriggerViaHttp.forwardMessage (aRequest);
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
  public static DcngIncomingHandlerViaHttp create (@Nonnull final String sLogPrefix)
  {
    // Check prerequisites
    if (StringHelper.hasNoText (DcngConfig.ME.getMEMIncomingURL ()))
      throw new IllegalStateException ("The MEM incoming URL for forwarding to DC/DP is not configured.");

    // go
    return new DcngIncomingHandlerViaHttp (sLogPrefix);
  }
}
