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
package com.helger.dcng.phase4.servlet;

import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.HttpHeaderMap;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.phase4.Phase4Config;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.phase4.CAS4;
import com.helger.phase4.attachment.AS4DecompressException;
import com.helger.phase4.attachment.WSS4JAttachment;
import com.helger.phase4.ebms3header.Ebms3Error;
import com.helger.phase4.ebms3header.Ebms3Property;
import com.helger.phase4.ebms3header.Ebms3SignalMessage;
import com.helger.phase4.ebms3header.Ebms3UserMessage;
import com.helger.phase4.error.EEbmsError;
import com.helger.phase4.messaging.IAS4IncomingMessageMetadata;
import com.helger.phase4.model.pmode.IPMode;
import com.helger.phase4.servlet.IAS4MessageState;
import com.helger.phase4.servlet.spi.AS4MessageProcessorResult;
import com.helger.phase4.servlet.spi.AS4SignalMessageProcessorResult;
import com.helger.phase4.servlet.spi.IAS4ServletMessageProcessorSPI;
import com.helger.xml.serialize.write.XMLWriter;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * DE4A specific implementation of {@link IAS4ServletMessageProcessorSPI}. It
 * takes incoming AS4 messages and forwards it accordingly to the correct DE4A
 * {@link IMEIncomingHandler}.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class AS4MessageProcessorSPI implements IAS4ServletMessageProcessorSPI
{
  public static final String ACTION_FAILURE = "Failure";
  private static final Logger LOGGER = LoggerFactory.getLogger (AS4MessageProcessorSPI.class);

  private static IMEIncomingHandler s_aIncomingHandler;

  public static void setIncomingHandler (@Nonnull final IMEIncomingHandler aIncomingHandler)
  {
    ValueEnforcer.notNull (aIncomingHandler, "IncomingHandler");
    ValueEnforcer.isNull (s_aIncomingHandler, "s_aIncomingHandler");
    s_aIncomingHandler = aIncomingHandler;
    LOGGER.info ("Set the incoming message handler to " + aIncomingHandler);
  }

  @Nullable
  private static IParticipantIdentifier _asPI (@Nullable final Ebms3Property aProp)
  {
    if (aProp == null)
      return null;

    final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();
    final String sType = aProp.getType ();
    final String sValue = aProp.getValue ();
    if (sType == null)
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("The particpant identifier is provided without a 'type' attribute: '" + sValue + "'");
      return aIF.parseParticipantIdentifier (sValue);
    }
    return aIF.createParticipantIdentifier (sType, sValue);
  }

  @Nonnull
  public AS4MessageProcessorResult processAS4UserMessage (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                                          @Nonnull final HttpHeaderMap aHttpHeaders,
                                                          @Nonnull final Ebms3UserMessage aUserMessage,
                                                          @Nonnull final IPMode aPMode,
                                                          @Nullable final Node aPayload,
                                                          @Nullable final ICommonsList <WSS4JAttachment> aIncomingAttachments,
                                                          @Nonnull final IAS4MessageState aState,
                                                          @Nonnull final ICommonsList <Ebms3Error> aProcessingErrors)
  {
    if (Phase4Config.isDebugLogIncoming () && LOGGER.isInfoEnabled ())
    {
      LOGGER.info ("Received AS4 message:");
      LOGGER.info ("  UserMessage: " + aUserMessage);
      LOGGER.info ("  Payload: " + (aPayload == null ? "null" : true ? "present" : XMLWriter.getNodeAsString (aPayload)));

      if (aIncomingAttachments != null)
      {
        LOGGER.info ("  Attachments: " + aIncomingAttachments.size ());
        for (final WSS4JAttachment x : aIncomingAttachments)
        {
          LOGGER.info ("    Attachment Content Type: " + x.getMimeType ());
          if (x.getMimeType ().startsWith ("text") || x.getMimeType ().endsWith ("/xml"))
          {
            try (final InputStream aIS = x.getSourceStream ())
            {
              LOGGER.info ("    Attachment Stream Class: " + aIS.getClass ().getName ());
              final String sContent = StreamHelper.getAllBytesAsString (x.getSourceStream (), x.getCharset ());
              LOGGER.info ("    Attachment Content: " + sContent.length () + " chars");
            }
            catch (final Exception ex)
            {
              LOGGER.warn ("    Attachment Content: CANNOT BE READ", ex);
            }
          }
        }
      }
    }

    if (aIncomingAttachments != null && aIncomingAttachments.isNotEmpty ())
    {
      try
      {
        final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();
        final ICommonsList <Ebms3Property> aProps = new CommonsArrayList <> (aUserMessage.getMessageProperties ().getProperty ());
        final Ebms3Property aPropOS = aProps.findFirst (x -> x.getName ().equals (CAS4.ORIGINAL_SENDER));
        final Ebms3Property aPropFR = aProps.findFirst (x -> x.getName ().equals (CAS4.FINAL_RECIPIENT));

        final MEMessage.Builder aMessageBuilder = MEMessage.builder ()
                                                           .senderID (_asPI (aPropOS))
                                                           .receiverID (_asPI (aPropFR))
                                                           .docTypeID (aIF.parseDocumentTypeIdentifier (aUserMessage.getCollaborationInfo ()
                                                                                                                    .getAction ()))
                                                           .processID (aIF.createProcessIdentifier (aUserMessage.getCollaborationInfo ()
                                                                                                                .getService ()
                                                                                                                .getType (),
                                                                                                    aUserMessage.getCollaborationInfo ()
                                                                                                                .getService ()
                                                                                                                .getValue ()));
        for (final WSS4JAttachment aItem : aIncomingAttachments)
          aMessageBuilder.addPayload (MEPayload.builder ()
                                               .mimeType (MimeTypeParser.safeParseMimeType (aItem.getMimeType ()))
                                               .contentID (aItem.getId ())
                                               .data (StreamHelper.getAllBytes (aItem.getSourceStream ())));

        s_aIncomingHandler.handleIncomingRequest (aMessageBuilder.build ());
      }
      catch (final AS4DecompressException ex)
      {
        final String sErrorMsg = "Error decompressing a compressed attachment";
        aProcessingErrors.add (EEbmsError.EBMS_DECOMPRESSION_FAILURE.getAsEbms3Error (aState.getLocale (),
                                                                                      aState.getMessageID (),
                                                                                      sErrorMsg));
        DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Error handling incoming AS4 message: " + sErrorMsg);
      }
      catch (final Exception ex)
      {
        DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Error handling incoming AS4 message", ex);
      }
    }

    // To test returning with a failure works as intended
    if (aUserMessage.getCollaborationInfo ().getAction ().equals (ACTION_FAILURE))
      return AS4MessageProcessorResult.createFailure (ACTION_FAILURE);

    // By default: success
    return AS4MessageProcessorResult.createSuccess ();
  }

  @Nonnull
  public AS4SignalMessageProcessorResult processAS4SignalMessage (@Nonnull final IAS4IncomingMessageMetadata aMessageMetadata,
                                                                  @Nonnull final HttpHeaderMap aHttpHeaders,
                                                                  @Nonnull final Ebms3SignalMessage aSignalMessage,
                                                                  @Nullable final IPMode aPmode,
                                                                  @Nonnull final IAS4MessageState aState,
                                                                  @Nonnull final ICommonsList <Ebms3Error> aProcessingErrors)
  {
    if (aSignalMessage.getReceipt () != null)
    {
      // Receipt - just acknowledge
      return AS4SignalMessageProcessorResult.createSuccess ();
    }

    if (!aSignalMessage.getError ().isEmpty ())
    {
      // Error - just acknowledge
      return AS4SignalMessageProcessorResult.createSuccess ();
    }

    return AS4SignalMessageProcessorResult.createSuccess ();
  }
}
