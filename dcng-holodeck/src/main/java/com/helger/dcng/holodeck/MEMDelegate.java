package com.helger.dcng.holodeck;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.NotThreadSafe;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.UsedViaReflection;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.url.URLHelper;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.me.incoming.MEIncomingException;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.dcng.holodeck.notifications.IMessageHandler;
import com.helger.dcng.holodeck.notifications.IRelayResultHandler;
import com.helger.dcng.holodeck.notifications.ISubmissionResultHandler;
import com.helger.dcng.holodeck.notifications.InternalRelayResultHandler;
import com.helger.dcng.holodeck.notifications.InternalSubmissionResultHandler;
import com.helger.dcng.holodeck.notifications.RelayResult;
import com.helger.dcng.holodeck.notifications.SubmissionResult;
import com.helger.scope.singleton.AbstractGlobalSingleton;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * The API Entry class for the Message Exchange API.
 *
 * @author myildiz
 */
@NotThreadSafe
public class MEMDelegate extends AbstractGlobalSingleton
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MEMDelegate.class);
  private final List <IMessageHandler> messageHandlers = new ArrayList <> ();
  private final List <IRelayResultHandler> relayResultHandlers = new ArrayList <> ();
  private final List <ISubmissionResultHandler> submissionResultHandlers = new ArrayList <> ();

  private final InternalRelayResultHandler internalRelayResultHandler;
  private final InternalSubmissionResultHandler internalSRHandler;

  @Deprecated
  @UsedViaReflection
  public MEMDelegate ()
  {
    // register this both as a submission result listener
    // and a notification listener in order to watch the process of
    // a send message call

    internalRelayResultHandler = new InternalRelayResultHandler ();
    internalSRHandler = new InternalSubmissionResultHandler ();

    relayResultHandlers.add (internalRelayResultHandler);
    submissionResultHandlers.add (internalSRHandler);
  }

  @Nonnull
  public static MEMDelegate getInstance ()
  {
    return getGlobalSingleton (MEMDelegate.class);
  }

  /**
   * The V1 message sending interface for the message exchange module
   *
   * @param gatewayRoutingMetadata
   *        The container for the endpoint information and docid/procid
   * @param meMessage
   *        the payloads and their metadata to be sent to the gateway.
   * @throws MEOutgoingException
   *         in case of error
   */
  public void sendMessage (final IMERoutingInformation gatewayRoutingMetadata, final MEMessage meMessage) throws MEOutgoingException
  {
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("Send message called for procid: " +
                    gatewayRoutingMetadata.getProcessID ().getURIEncoded () +
                    " docid: " +
                    gatewayRoutingMetadata.getDocumentTypeID ().getURIEncoded ());
      LOGGER.debug ("Convert gateway routing metadata to submission data");
    }
    final SubmissionMessageProperties submissionData = EBMSUtils.inferSubmissionData (gatewayRoutingMetadata);
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Create SOAP Message based on the submission data and the payloads");
    final SOAPMessage soapMessage = EBMSUtils.convert2MEOutboundAS4Message (submissionData, meMessage);
    if (LOGGER.isTraceEnabled ())
    {
      LOGGER.trace (SoapUtil.describe (soapMessage));
    }

    String messageID;
    try
    {
      messageID = SoapXPathUtil.getSingleNodeTextContent (soapMessage.getSOAPHeader (), "//:MessageInfo/:MessageId");
    }
    catch (final SOAPException e)
    {
      throw new MEOutgoingException ("Failed to find MessageId", e);
    }

    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("New soap message ID " + messageID);
      LOGGER.debug ("Send soap message " + messageID);
    }

    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("\n" + SoapUtil.describe (soapMessage));
    }

    EBMSUtils.sendSOAPMessage (soapMessage, URLHelper.getAsURL (MEMHolodeckConfig.getMEMAS4Endpoint ()));
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("SOAP Message " + messageID + " sent");

    final long timeout = MEMHolodeckConfig.getGatewayNotificationWaitTimeout ();
    // now that we have sent the object, first wait for the submission result
    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Wait for SubmissionResult for " + messageID);
    final SubmissionResult submissionResult = (SubmissionResult) internalSRHandler.obtainNotification (messageID, timeout);

    LOGGER.info ("SubmissionResult " + submissionResult.getResult ());
    if (submissionResult.getResult () != EResultType.RECEIPT)
    {
      if (LOGGER.isErrorEnabled ())
      {
        LOGGER.error ("SubmitMessageId: " + submissionResult.getErrorCode ());
        LOGGER.error ("C2-C3 MessageId: " + submissionResult.getRefToMessageID ());
        LOGGER.error ("ErrorCode: " + submissionResult.getErrorCode ());
        LOGGER.error ("Description: " + submissionResult.getDescription ());
      }

      final String errorMesage = "Error from AS4 transmission: EToopErrorCode.ME_002 -- " +
                                 "EBMS ERROR CODE: [" +
                                 submissionResult.getErrorCode () +
                                 "]\n";

      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> errorMesage);
      throw new MEOutgoingException (EDcngErrorCode.ME_002, errorMesage);
    }

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Wait for RelayResult for " + messageID);
    final RelayResult relayResult = (RelayResult) internalRelayResultHandler.obtainNotification (submissionResult.getMessageID (), timeout);

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("RelayResult " + relayResult.getResult ());

    if (relayResult.getResult () != EResultType.RECEIPT)
    {
      if (LOGGER.isErrorEnabled ())
      {
        LOGGER.error ("SubmitMessageId: " + relayResult.getErrorCode ());
        LOGGER.error ("C2-C3 MessageId: " + relayResult.getRefToMessageID ());
        LOGGER.error ("ErrorCode: " + relayResult.getErrorCode ());
        LOGGER.error ("Severity: " + relayResult.getSeverity ());
        LOGGER.error ("ShortDescription: " + relayResult.getShortDescription ());
        LOGGER.error ("Description: " + relayResult.getDescription ());
      }

      final String errorMesage = "Error from AS4 transmission: " +
                                 "EBMS ERROR CODE: " +
                                 relayResult.getErrorCode () +
                                 "\nSeverity: " +
                                 relayResult.getSeverity () +
                                 "\nShort Description: " +
                                 relayResult.getShortDescription ();

      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> errorMesage);
      if ("EBMS:0301".equals (relayResult.getErrorCode ()))
        throw new MEOutgoingException (EDcngErrorCode.ME_003, errorMesage);
      throw new MEOutgoingException (EDcngErrorCode.ME_004, errorMesage);
    }
  }

  /**
   * Register a new message handler to handle the inbound messages from the AS4
   * gateway.
   * <p>
   * Duplicate checking skipped for now. So if you register a handler twice, its
   * handle method will be called twice.
   *
   * @param aMessageHandler
   *        message handler to be added
   */
  public void registerMessageHandler (@Nonnull final IMessageHandler aMessageHandler)
  {
    ValueEnforcer.notNull (aMessageHandler, "MessageHandler");
    messageHandlers.add (aMessageHandler);
  }

  /**
   * Remove a message handler from this delegate
   *
   * @param aMessageHandler
   *        Message handler to be removed
   */
  public void deregisterMessageHandler (@Nonnull final IMessageHandler aMessageHandler)
  {
    ValueEnforcer.notNull (aMessageHandler, "MessageHandler");
    messageHandlers.remove (aMessageHandler);
  }

  /**
   * Register a new notification handler to handle the notifications.
   * <p>
   * Duplicate checking skipped for now. So if you register a handler twice, its
   * handle method will be called twice.
   *
   * @param notificationHandler
   *        message handler to be added
   */
  public void registerNotificationHandler (@Nonnull final IRelayResultHandler notificationHandler)
  {
    ValueEnforcer.notNull (notificationHandler, "NotificationHandler");
    relayResultHandlers.add (notificationHandler);
  }

  /**
   * Remove a notification handler from this delegate
   *
   * @param notificationHandler
   *        Message handler to be removed
   */
  public void deregisterNotificationHandler (@Nonnull final IRelayResultHandler notificationHandler)
  {
    ValueEnforcer.notNull (notificationHandler, "NotificationHandler");
    relayResultHandlers.remove (notificationHandler);
  }

  /**
   * Register a new submission result handler to handle the SubmissionResult's.
   * <p>
   * Duplicate checking skipped for now. So if you register a handler twice, its
   * handle method will be called twice.
   *
   * @param submissionResultHandler
   *        submission result handler to be added
   */
  public void registerSubmissionResultHandler (@Nonnull final ISubmissionResultHandler submissionResultHandler)
  {
    ValueEnforcer.notNull (submissionResultHandler, "SubmissionResultHandler");
    submissionResultHandlers.add (submissionResultHandler);
  }

  /**
   * Remove a submissionResultHandler handler from this delegate
   *
   * @param submissionResultHandler
   *        submissionResultHandler to be removed
   */
  public void deregisterSubmissionResultHandler (@Nonnull final ISubmissionResultHandler submissionResultHandler)
  {
    ValueEnforcer.notNull (submissionResultHandler, "SubmissionResultHandler");
    submissionResultHandlers.remove (submissionResultHandler);
  }

  /**
   * Dispatch the received inbound message form the AS4 gateway to the handlers
   *
   * @param message
   *        message to be dispatched
   * @throws MEIncomingException
   *         in case of error
   */
  public void dispatchInboundMessage (@Nonnull final SOAPMessage message) throws MEIncomingException
  {
    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Received a Deliver message\n" + //
                   "   Inbound  AS4  Message ID: " +
                   EBMSUtils.getMessageId (message));
    try
    {
      // Do it only once
      final MEMessage aMEMessage = EBMSUtils.soap2MEMessage (message);
      for (final IMessageHandler messageHandler : messageHandlers)
      {
        messageHandler.handleMessage (aMEMessage);
      }
    }
    catch (final Exception e)
    {
      throw new MEIncomingException ("Error handling message " + message, e);
    }
  }

  /**
   * Dispatch the received RelayResult to the registered listeners
   *
   * @param notification
   *        Relay result
   * @throws MEIncomingException
   *         in case of error
   */
  public void dispatchRelayResult (final SOAPMessage notification) throws MEIncomingException
  {
    try
    {
      // Do it only once
      final RelayResult relayResult = EBMSUtils.soap2RelayResult (notification);

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("RelayResult for \n" + //
                     "   Outbound AS4  Message ID: " +
                     relayResult.getRefToMessageID () +
                     "\n" + //
                     "   MessageID:                " +
                     relayResult.getMessageID ());

      for (final IRelayResultHandler notificationHandler : relayResultHandlers)
      {
        notificationHandler.handleNotification (relayResult);
      }
    }
    catch (final Exception e)
    {
      throw new MEIncomingException ("Error handling message " + notification, e);
    }
  }

  /**
   * Dispatch the received SubmissioNResult to the registered listeners
   *
   * @param submissionResult
   *        Submission result SOAP message
   * @throws MEIncomingException
   *         in case of error
   */
  public void dispatchSubmissionResult (final SOAPMessage submissionResult) throws MEIncomingException
  {
    try
    {
      // Do it only once
      final SubmissionResult sSubmissionResult = EBMSUtils.soap2SubmissionResult (submissionResult);

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("SubmissionResult for \n" + //
                     "   SubmitMessage Message ID: " +
                     sSubmissionResult.getRefToMessageID () +
                     "\n" +
                     "   Outbound AS4  Message ID: " +
                     sSubmissionResult.getMessageID ());

      for (final ISubmissionResultHandler submissionResultHandler : submissionResultHandlers)
      {
        submissionResultHandler.handleSubmissionResult (sSubmissionResult);
      }
    }
    catch (final Exception e)
    {
      throw new MEIncomingException ("Error handling message " + submissionResult, e);
    }
  }
}
