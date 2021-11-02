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
package com.helger.dcng.holodeck;

import java.net.URL;
import java.nio.charset.Charset;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Locale;
import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import javax.xml.XMLConstants;
import javax.xml.bind.DatatypeConverter;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.charset.CharsetHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.mime.MimeType;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.mime.MimeTypeParserException;
import com.helger.commons.regex.RegExHelper;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.me.incoming.MEIncomingException;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.dcng.holodeck.notifications.RelayResult;
import com.helger.dcng.holodeck.notifications.SubmissionResult;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.xml.microdom.IMicroDocument;
import com.helger.xml.microdom.IMicroElement;
import com.helger.xml.microdom.MicroDocument;
import com.helger.xml.microdom.MicroElement;
import com.helger.xml.microdom.serialize.MicroWriter;
import com.helger.xml.namespace.MapBasedNamespaceContext;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.write.XMLWriterSettings;
import com.helger.xml.transform.TransformSourceFactory;

import eu.de4a.kafkaclient.DE4AKafkaClient;

public final class EBMSUtils
{

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger (EBMSUtils.class);
  // SOAP 1.2 NS
  public static final String NS_SOAPENV = "http://www.w3.org/2003/05/soap-envelope";
  public static final String NS_EBMS = "http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/ns/core/200704/";

  private EBMSUtils ()
  {}

  /*
   * See
   * http://docs.oasis-open.org/ebxml-msg/ebms/v3.0/profiles/AS4-profile/v1.0/
   * os/ AS4-profile-v1.0-os.html#__RefHeading__26454_1909778835
   */
  public static byte [] createSuccessReceipt (final SOAPMessage message) throws MEIncomingException
  {
    ValueEnforcer.notNull (message, "SOAPMessage");

    try
    {
      final StreamSource stylesource = TransformSourceFactory.create (new ClassPathResource ("/receipt-generator.xslt"));
      final TransformerFactory transformerFactory = TransformerFactory.newInstance ();
      transformerFactory.setFeature (XMLConstants.FEATURE_SECURE_PROCESSING, true);
      final Transformer transformer = transformerFactory.newTransformer (stylesource);
      transformer.setParameter ("messageid", genereateEbmsMessageId (MEMConstants.MEM_AS4_SUFFIX));
      transformer.setParameter ("timestamp", DateTimeUtils.getCurrentTimestamp ());
      try (final NonBlockingByteArrayOutputStream baos = new NonBlockingByteArrayOutputStream ())
      {
        transformer.transform (new DOMSource (message.getSOAPPart ()), new StreamResult (baos));
        return baos.toByteArray ();
      }
    }
    catch (final RuntimeException | TransformerException ex)
    {
      // force exceptions to runtime
      throw new MEIncomingException ("Failed to create success receipt", ex);
    }
  }

  /**
   * Create a fault message based on the error message
   *
   * @param soapMessage
   *        Source SOAP message. May be <code>null</code>
   * @param faultMessage
   *        Fault message. May be <code>null</code>.
   * @return byte[] with result XML SOAP Fault message
   * @throws MEIncomingException
   *         in case of error
   */
  public static byte [] createFault (@Nullable final SOAPMessage soapMessage,
                                     @Nullable final String faultMessage) throws MEIncomingException
  {
    final String fm = faultMessage != null ? faultMessage : "Unknown Error";
    String refToMessageInError;

    if (soapMessage != null)
    {
      refToMessageInError = getMessageId (soapMessage);
    }
    else
    {
      refToMessageInError = "";
    }

    final IMicroDocument aDoc = new MicroDocument ();
    final IMicroElement eEnvelope = aDoc.appendElement (NS_SOAPENV, "Envelope");
    {
      final IMicroElement eHeader = eEnvelope.appendElement (NS_SOAPENV, "Head");
      final IMicroElement eMessaging = eHeader.appendElement (NS_EBMS, "Messaging");
      final IMicroElement eSignalMessage = eMessaging.appendElement (NS_EBMS, "SignalMessage");
      {
        final IMicroElement eMessageInfo = eSignalMessage.appendElement (NS_EBMS, "MessageInfo");
        eMessageInfo.appendElement (NS_EBMS, "Timestamp").appendText (DateTimeUtils.getCurrentTimestamp ());
        final String ebmsMessageId = genereateEbmsMessageId (MEMConstants.MEM_AS4_SUFFIX);
        eMessageInfo.appendElement (NS_EBMS, "MessageId").appendText (ebmsMessageId);
      }
      {
        final IMicroElement eError = eSignalMessage.appendElement (NS_EBMS, "Error");
        eError.setAttribute ("category", "CONTENT");
        eError.setAttribute ("errorCode", "EBMS:0004");
        eError.setAttribute ("origin", "ebms");
        eError.setAttribute ("refToMessageInError", refToMessageInError);
        eError.setAttribute ("severity", "failure");
        eError.setAttribute ("shortDescription", "Error");
        eError.appendElement (NS_EBMS, "Description").setAttribute (XMLConstants.XML_NS_URI, "lang", "en").appendText (fm);
        eError.appendElement (NS_EBMS, "ErrorDetail").appendText (fm);
      }
    }
    {
      final IMicroElement eBody = eEnvelope.appendElement (NS_SOAPENV, "Body");
      final IMicroElement eFault = eBody.appendElement (NS_SOAPENV, "Fault");
      {
        final IMicroElement eCode = eFault.appendElement (NS_SOAPENV, "Code");
        eCode.appendElement (NS_SOAPENV, "Value").appendText ("env:Receiver");
      }
      {
        final IMicroElement eReason = eFault.appendElement (NS_SOAPENV, "Reason");
        eReason.appendElement (NS_SOAPENV, "Text").setAttribute (XMLConstants.XML_NS_URI, "lang", "en").appendText (fm);
      }
    }

    final MapBasedNamespaceContext aNSCtx = new MapBasedNamespaceContext ();
    aNSCtx.addMapping ("env", NS_SOAPENV);
    aNSCtx.addMapping ("eb", NS_EBMS);

    return MicroWriter.getNodeAsBytes (aDoc, new XMLWriterSettings ().setNamespaceContext (aNSCtx));
  }

  /**
   * Generate a random ebms message id with the format
   * <code>&lt;RANDOM UUID&gt;@ext</code>
   *
   * @param ext
   *        suffix to use. May not be <code>null</code>.
   * @return EBMS Message ID. Never <code>null</code> nor empty.
   */
  public static String genereateEbmsMessageId (final String ext)
  {
    return UUID.randomUUID ().toString () + "@" + ext;
  }

  @Nullable
  private static IMicroElement _property (@Nonnull final String sName, @Nullable final String sValue)
  {
    if (sValue == null)
    {
      return null;
    }

    final IMicroElement ret = new MicroElement (NS_EBMS, "Property");
    ret.setAttribute ("name", sName).appendText (sValue);
    return ret;
  }

  /*
   * The conversion procedure goes here
   */
  public static SOAPMessage convert2MEOutboundAS4Message (final SubmissionMessageProperties metadata,
                                                          final MEMessage meMessage) throws MEOutgoingException
  {
    if (LOG.isDebugEnabled ())
    {
      LOG.debug ("Convert submission data to SOAP Message");
    }

    try
    {
      final IMicroDocument aDoc = new MicroDocument ();
      final IMicroElement eMessaging = aDoc.appendElement (NS_EBMS, "Messaging");
      eMessaging.setAttribute (NS_SOAPENV, "mustUnderstand", "true");
      final IMicroElement eUserMessage = eMessaging.appendElement (NS_EBMS, "UserMessage");

      {
        final IMicroElement eMessageInfo = eUserMessage.appendElement (NS_EBMS, "MessageInfo");
        eMessageInfo.appendElement (NS_EBMS, "Timestamp").appendText (DateTimeUtils.getCurrentTimestamp ());
        final String ebmsMessageId = genereateEbmsMessageId (MEMConstants.MEM_AS4_SUFFIX);
        eMessageInfo.appendElement (NS_EBMS, "MessageId").appendText (ebmsMessageId);
      }
      {
        final IMicroElement ePartyInfo = eUserMessage.appendElement (NS_EBMS, "PartyInfo");
        {
          final IMicroElement eFrom = ePartyInfo.appendElement (NS_EBMS, "From");
          final IMicroElement partyId = eFrom.appendElement (NS_EBMS, "PartyId");
          partyId
                 // .setAttribute("type",
                 // "urn:oasis:names:tc:ebcore:partyid-type:unregistered")
                 .appendText (MEMHolodeckConfig.getMEMAS4TcPartyid ());
          eFrom.appendElement (NS_EBMS, "Role").appendText (MEMConstants.MEM_PARTY_ROLE);
        }
        {
          final IMicroElement eTo = ePartyInfo.appendElement (NS_EBMS, "To");
          eTo.appendElement (NS_EBMS, "PartyId")
             // .setAttribute("type",
             // "urn:oasis:names:tc:ebcore:partyid-type:unregistered")
             .appendText (MEMHolodeckConfig.getMEMAS4GwPartyID ());
          eTo.appendElement (NS_EBMS, "Role").appendText (MEMConstants.GW_PARTY_ROLE);
        }
      }

      {
        final IMicroElement eCollaborationInfo = eUserMessage.appendElement (NS_EBMS, "CollaborationInfo");
        eCollaborationInfo.appendElement (NS_EBMS, "Service").appendText (MEMConstants.SERVICE);
        eCollaborationInfo.appendElement (NS_EBMS, "Action").appendText (MEMConstants.ACTION_SUBMIT);
        eCollaborationInfo.appendElement (NS_EBMS, "ConversationId").appendText (metadata.conversationId);
      }

      {
        final IMicroElement eMessageProperties = eUserMessage.appendElement (NS_EBMS, "MessageProperties");
        eMessageProperties.appendChild (_property ("ToPartyId", metadata.toPartyId));
        eMessageProperties.appendChild (_property ("ToPartyIdType", metadata.toPartyIdType));
        eMessageProperties.appendChild (_property ("ToPartyRole", metadata.toPartyRole));
        // NOTE: ToPartyCertificate is the DER+BASE64 encoded X509 certificate.
        // First decode as byte array, then parse it using
        // CertificateFactory.getInstance("X509", "BC")
        // recommended provider: BouncyCastleProvider
        eMessageProperties.appendChild (_property ("ToPartyCertificate", metadata.toPartyCertificate));
        eMessageProperties.appendChild (_property ("TargetURL", metadata.targetURL));
        eMessageProperties.appendChild (_property ("Service", metadata.service));
        eMessageProperties.appendChild (_property ("ServiceType", metadata.serviceType));
        eMessageProperties.appendChild (_property ("Action", metadata.action));

        eMessageProperties.appendChild (_property ("MessageId", metadata.messageId));
        eMessageProperties.appendChild (_property ("RefToMessageId", metadata.refToMessageId));
        eMessageProperties.appendChild (_property ("ConversationId", metadata.conversationId));

        // split in type and value is not desired
        eMessageProperties.appendChild (_property ("originalSender",
                                                   metadata.senderId != null ? metadata.senderId.getURIEncoded () : null));
        eMessageProperties.appendChild (_property ("finalRecipient",
                                                   metadata.receiverId != null ? metadata.receiverId.getURIEncoded () : null));
      }

      {
        final IMicroElement ePayloadInfo = eUserMessage.appendElement (NS_EBMS, "PayloadInfo");
        for (final MEPayload aPayload : meMessage.payloads ())
        {
          final IMicroElement ePartInfo = ePayloadInfo.appendElement (NS_EBMS, "PartInfo");
          ePartInfo.setAttribute ("href", "cid:" + aPayload.getContentID ());

          final IMicroElement ePartProperties = ePartInfo.appendElement (NS_EBMS, "PartProperties");
          ePartProperties.appendChild (_property ("MimeType", aPayload.getMimeTypeString ()));
        }
      }

      final MapBasedNamespaceContext aNSCtx = new MapBasedNamespaceContext ();
      aNSCtx.addMapping ("env", NS_SOAPENV);
      aNSCtx.addMapping ("eb", NS_EBMS);

      // Convert to org.w3c.dom ....
      final Element element = DOMReader.readXMLDOM (MicroWriter.getNodeAsBytes (aDoc,
                                                                                new XMLWriterSettings ().setNamespaceContext (aNSCtx)))
                                       .getDocumentElement ();

      // create a soap message based on this XML
      final SOAPMessage message = SoapUtil.createEmptyMessage ();
      final Node importNode = message.getSOAPHeader ().getOwnerDocument ().importNode (element, true);
      message.getSOAPHeader ().appendChild (importNode);

      for (final MEPayload payload : meMessage.payloads ())
      {
        final AttachmentPart attachmentPart = message.createAttachmentPart ();
        attachmentPart.setContentId ('<' + payload.getContentID () + '>');
        try
        {
          attachmentPart.setRawContentBytes (payload.getData ().bytes (),
                                             payload.getData ().getOffset (),
                                             payload.getData ().size (),
                                             payload.getMimeTypeString ());
        }
        catch (final SOAPException e)
        {
          throw new MEOutgoingException ("Failed to read payload", e);
        }
        message.addAttachmentPart (attachmentPart);
      }

      if (message.saveRequired ())
      {
        message.saveChanges ();
      }

      if (LOG.isTraceEnabled ())
      {
        LOG.trace (SoapUtil.describe (message));
      }
      return message;
    }
    catch (final RuntimeException |

           SOAPException ex)
    {
      throw new MEOutgoingException ("Unspecific error", ex);
    }
  }

  /**
   * Process the inbound SOAPMessage and convert it to the MEMEssage
   *
   * @param message
   *        the soap message to be converted to a MEMessage. Cannot be null
   * @return the MEMessage object created from the supplied SOAPMessage
   * @throws MEIncomingException
   *         in case of error
   */
  public static MEMessage soap2MEMessage (@Nonnull final SOAPMessage message) throws MEIncomingException
  {
    ValueEnforcer.notNull (message, "SOAPMessage");

    if (LOG.isDebugEnabled ())
    {
      LOG.debug ("Convert message to submission data");
    }

    final MEMessage.Builder meMessage = MEMessage.builder ();

    // load the message properties
    final SOAPHeader soapHeader;
    try
    {
      soapHeader = message.getSOAPHeader ();
    }
    catch (final SOAPException e)
    {
      throw new MEIncomingException (e.getMessage (), e);
    }

    if (message.countAttachments () > 0)
    {
      // Read all attachments
      final Iterator <?> it = message.getAttachments ();
      while (it.hasNext ())
      {
        final AttachmentPart att = (AttachmentPart) it.next ();
        // remove surplus characters
        final String href = RegExHelper.stringReplacePattern ("<|>", att.getContentId (), "");
        final Node partInfo;
        try
        {
          // throws exception if part info does not exist
          partInfo = SoapXPathUtil.safeFindSingleNode (soapHeader, "//:PayloadInfo/:PartInfo[@href='cid:" + href + "']");
        }
        catch (final Exception ex)
        {
          throw new MEIncomingException ("ContentId: " + href + " was not found in PartInfo");
        }

        String sMimeType = SoapXPathUtil.getSingleNodeTextContent (partInfo, ".//:PartProperties/:Property[@name='MimeType']");
        if (sMimeType.startsWith ("cid:"))
        {
          sMimeType = sMimeType.substring (4);
        }

        MimeType mimeType;
        try
        {
          mimeType = MimeTypeParser.parseMimeType (sMimeType);
        }
        catch (final MimeTypeParserException ex)
        {
          LOG.warn ("Error parsing MIME type '" + sMimeType + "': " + ex.getMessage ());
          // if there is a problem wrt the processing of the mimetype, simply
          // grab the
          // content type
          try
          {
            mimeType = MimeTypeParser.parseMimeType (att.getContentType ());
          }
          catch (final MimeTypeParserException ex2)
          {
            LOG.warn ("Error parsing fallback MIME type '" + att.getContentType () + "': " + ex2.getMessage ());
            mimeType = new MimeType (CMimeType.APPLICATION_OCTET_STREAM);
          }
        }

        try
        {
          final Node charSetNode = SoapXPathUtil.findSingleNode (partInfo, ".//:PartProperties/:Property[@name='CharacterSet']/text()");
          if (charSetNode != null)
          {
            final Charset aCharset = CharsetHelper.getCharsetFromNameOrNull (charSetNode.getNodeValue ());
            if (aCharset != null)
            {
              // Add charset to MIME type
              mimeType.addParameter (CMimeType.PARAMETER_NAME_CHARSET, aCharset.name ());
            }
          }
        }
        catch (final RuntimeException ex)
        {
          // ignore
        }

        byte [] rawContentBytes;
        try
        {
          rawContentBytes = att.getRawContentBytes ();
        }
        catch (final SOAPException e)
        {
          throw new MEIncomingException ("Failed to read payload", e);
        }

        final MEPayload payload = MEPayload.builder ().mimeType (mimeType).contentID (href).data (rawContentBytes).build ();
        if (LOG.isDebugEnabled ())
        {
          LOG.debug ("\tpayload.payloadId: " + payload.getContentID ());
          LOG.debug ("\tpayload.mimeType: " + payload.getMimeTypeString ());
        }

        meMessage.addPayload (payload);
      }
    }

    final Node messagePropsNode = SoapXPathUtil.safeFindSingleNode (soapHeader, "//:MessageProperties");

    String sSenderIdType;
    try
    {
      sSenderIdType = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='originalSender']/@type");
    }
    catch (final IllegalArgumentException ex)
    {
      sSenderIdType = null;
    }
    final String sSenderId = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='originalSender']/text()");

    String sReceiverIdType;
    try
    {
      sReceiverIdType = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='finalRecipient']/@type");
    }
    catch (final IllegalArgumentException ex)
    {
      sReceiverIdType = null;
    }
    final String sReceiverId = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='finalRecipient']/text()");

    // Document can never have a type attribute
    final String sDoctypeId = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Action']/text()");

    // For RC2 backwards compatibility
    String sProcidType;
    try
    {
      sProcidType = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='ServiceType']/text()");
    }
    catch (final IllegalArgumentException ex)
    {
      sProcidType = null;
    }
    final String sProcid = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Service']/text()");

    final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();
    final IParticipantIdentifier sender = sSenderIdType != null ? aIF.createParticipantIdentifier (sSenderIdType, sSenderId)
                                                                : aIF.parseParticipantIdentifier (sSenderId);
    if (sender == null)
      LOG.warn ("Failed to create/parse sender participant identifier '" + sSenderIdType + "' and '" + sSenderId + "'");
    final IParticipantIdentifier receiver = sReceiverIdType != null ? aIF.createParticipantIdentifier (sReceiverIdType, sReceiverId)
                                                                    : aIF.parseParticipantIdentifier (sReceiverId);
    if (receiver == null)
      LOG.warn ("Failed to create/parse receiver participant identifier '" + sReceiverIdType + "' and '" + sReceiverId + "'");
    final IDocumentTypeIdentifier doctypeid = aIF.parseDocumentTypeIdentifier (sDoctypeId);
    if (doctypeid == null)
      LOG.warn ("Failed to parse document type identifier '" + sDoctypeId + "'");
    final IProcessIdentifier procid = sProcidType != null ? aIF.createProcessIdentifier (sProcidType, sProcid)
                                                          : aIF.parseProcessIdentifier (sProcid);
    if (procid == null)
      LOG.warn ("Failed to create/parse process identifier '" + sProcidType + "' and '" + sProcid + "'");

    return meMessage.senderID (sender).receiverID (receiver).processID (procid).docTypeID (doctypeid).build ();
  }

  public static RelayResult soap2RelayResult (final SOAPMessage sNotification) throws MEIncomingException
  {
    ValueEnforcer.notNull (sNotification, "Notification");

    final RelayResult notification = new RelayResult ();

    try
    {
      final Node messagePropsNode = SoapXPathUtil.safeFindSingleNode (sNotification.getSOAPHeader (), "//:MessageProperties");

      final String messageId = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='MessageId']/text()");
      notification.setMessageID (messageId);

      final String refToMessageId = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode,
                                                                            ".//:Property[@name='RefToMessageId']/text()");
      notification.setRefToMessageID (refToMessageId);

      final String sSignalType = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Result']");
      if (!"ERROR".equalsIgnoreCase (sSignalType))
      {
        notification.setResult (EResultType.RECEIPT);
      }
      else
      {
        notification.setResult (EResultType.ERROR);

        try
        {
          final String errorCode = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='ErrorCode']");
          notification.setErrorCode (errorCode);
        }
        catch (final RuntimeException e)
        {
          throw new IllegalStateException ("ErrorCode is mandatory for relay result errors.");
        }

        try
        {
          final String severity = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='severity']");
          notification.setSeverity (severity);
        }
        catch (final RuntimeException e)
        {
          // TODO so what?
        }

        try
        {
          final String shortDesc = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='ShortDescription']");
          notification.setShortDescription (shortDesc);
        }
        catch (final RuntimeException ignored)
        {
          // TODO so what?
        }

        try
        {
          final String desc = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Description']");
          notification.setDescription (desc);
        }
        catch (final RuntimeException ignored)
        {
          // TODO so what?
        }
      }

      return notification;
    }
    catch (final RuntimeException ex)
    {
      throw ex;
    }
    catch (final SOAPException ex)
    {
      throw new MEIncomingException ("SOAP error", ex);
    }
  }

  public static SubmissionResult soap2SubmissionResult (final SOAPMessage sSubmissionResult) throws MEIncomingException
  {
    ValueEnforcer.notNull (sSubmissionResult, "SubmissionResult");

    final SubmissionResult submissionResult = new SubmissionResult ();

    try
    {
      final Node messagePropsNode = SoapXPathUtil.safeFindSingleNode (sSubmissionResult.getSOAPHeader (), "//:MessageProperties");

      final String refToMessageID = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode,
                                                                            ".//:Property[@name='RefToMessageId']/text()");
      submissionResult.setRefToMessageID (refToMessageID);

      final String sSignalType = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Result']");
      if ("ERROR".equalsIgnoreCase (sSignalType))
      {
        submissionResult.setResult (EResultType.ERROR);

        // description must be there when there is an error
        final String description = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='Description']");
        submissionResult.setDescription (description);

      }
      else
      {
        submissionResult.setResult (EResultType.RECEIPT);

        // message id is conditional, it must be there only in case of receipt
        final String messageID = SoapXPathUtil.getSingleNodeTextContent (messagePropsNode, ".//:Property[@name='MessageId']/text()");
        submissionResult.setMessageID (messageID);
      }

      return submissionResult;
    }
    catch (final RuntimeException ex)
    {
      throw ex;
    }
    catch (final Exception ex)
    {
      throw new MEIncomingException ("SOAP error", ex);
    }
  }

  @Nonnull
  private static String _getCN (@Nonnull final String sPrincipal)
  {
    try
    {
      for (final Rdn aRdn : new LdapName (sPrincipal).getRdns ())
      {
        if (aRdn.getType ().equalsIgnoreCase ("CN"))
        {
          return (String) aRdn.getValue ();
        }
      }
      throw new IllegalStateException ("Failed to get CN from '" + sPrincipal + "'");
    }
    catch (final InvalidNameException ex)
    {
      throw new IllegalStateException ("Failed to get CN from '" + sPrincipal + "'", ex);
    }
  }

  /**
   * process the GatewayRoutingMetadata object and obtain the actual submission
   * data. Use for any direction (DC -&gt; DP and DP -&gt; DC)
   *
   * @return SubmissionData
   */
  static SubmissionMessageProperties inferSubmissionData (final IMERoutingInformation gatewayRoutingMetadata) throws MEOutgoingException
  {
    final X509Certificate certificate = gatewayRoutingMetadata.getCertificate ();
    // we need the certificate to obtain the to party id
    ValueEnforcer.notNull (certificate, "Endpoint Certificate");
    final SubmissionMessageProperties submissionData = new SubmissionMessageProperties ();
    submissionData.messageId = genereateEbmsMessageId (MEMConstants.MEM_AS4_SUFFIX);
    submissionData.action = gatewayRoutingMetadata.getDocumentTypeID ().getURIEncoded ();
    submissionData.service = gatewayRoutingMetadata.getProcessID ().getValue ();
    submissionData.serviceType = gatewayRoutingMetadata.getProcessID ().getScheme ();

    submissionData.toPartyId = _getCN (gatewayRoutingMetadata.getCertificate ().getSubjectX500Principal ().getName ());

    submissionData.toPartyIdType = MEMHolodeckConfig.getToPartyIdType ();

    // TODO: infer it from the transaction id
    submissionData.conversationId = "1";

    // this is the role of the RECEIVING gateway, it has been set
    // to the role of the SENDING gateway (i.e MEMConstants.GW_PARTY_ROLE)
    submissionData.toPartyRole = MEMConstants.GW_PARTY_ROLE;

    submissionData.targetURL = gatewayRoutingMetadata.getEndpointURL ();

    submissionData.senderId = gatewayRoutingMetadata.getSenderID ();
    submissionData.receiverId = gatewayRoutingMetadata.getReceiverID ();

    try
    {
      // DER encoded X509 certificate
      final byte [] certBytes = gatewayRoutingMetadata.getCertificate ().getEncoded ();
      // base 64 encoded DER bytes (i.e. converted to CER)
      submissionData.toPartyCertificate = DatatypeConverter.printBase64Binary (certBytes);
    }
    catch (final CertificateEncodingException e)
    {
      throw new MEOutgoingException ("Certificate interpreation error", e);
    }
    return submissionData;
  }

  /**
   * Calls {@link SoapUtil#sendSOAPMessage(SOAPMessage, URL)} but checks the
   * return value for a fault or a receipt.
   *
   * @param soapMessage
   *        Message to be sent. May not be <code>null</code>
   * @param url
   *        Target URL. May not be <code>null</code>
   * @throws MEOutgoingException
   *         if a fault is received instead of an ebms receipt
   */
  public static void sendSOAPMessage (@Nonnull final SOAPMessage soapMessage, @Nonnull final URL url) throws MEOutgoingException
  {
    ValueEnforcer.notNull (soapMessage, "SOAP Message");
    ValueEnforcer.notNull (url, "Target url");

    if (LOG.isTraceEnabled ())
    {
      LOG.trace (SoapUtil.describe (soapMessage));
    }
    final SOAPMessage response = SoapUtil.sendSOAPMessage (soapMessage, url);

    if (response != null)
    {
      if (LOG.isTraceEnabled ())
      {
        LOG.trace (SoapUtil.describe (response));
      }
      validateReceipt (response);
    } // else the receipt is null and we received a HTTP.OK, isn't that great?
  }

  /**
   * Check if the response is a soap fault (i.e. the response contains an error)
   */
  private static void validateReceipt (@Nonnull final SOAPMessage response) throws MEOutgoingException
  {

    ValueEnforcer.notNull (response, "Soap message");
    final Element errorElement = (Element) SoapXPathUtil.findSingleNode (response.getSOAPPart (), "//:SignalMessage/:Error");

    if (errorElement != null)
    {
      final String cat = StringHelper.getNotNull (errorElement.getAttribute ("category")).toUpperCase (Locale.US);
      final String shortDescription = StringHelper.getNotNull (errorElement.getAttribute ("shortDescription")).toUpperCase (Locale.US);
      final String severity = StringHelper.getNotNull (errorElement.getAttribute ("severity")).toUpperCase (Locale.US);
      final String code = StringHelper.getNotNull (errorElement.getAttribute ("errorCode")).toUpperCase (Locale.US);

      final StringBuilder errBuff = new StringBuilder ();
      errBuff.append ("EBMS ERROR CODE: [" + code + "]\n");
      errBuff.append ("Severity: [" + severity + "]\n");
      errBuff.append ("Category: [" + cat + "]\n");
      errBuff.append ("ShortDescription: [" + shortDescription + "]\n");
      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Error from AS4 transmission: EDcngErrorCode.ME_002 -- " + errBuff.toString ());
      throw new MEOutgoingException (EDcngErrorCode.ME_002, errBuff.toString ());
    }

    // Short info that it worked
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "AS4 transmission seemed to have worked out fine");
  }

  /**
   * Find the //:MessageInfo/eb:MessageId.text and return it.
   *
   * @param soapMessage
   *        SOAP message to extract data from
   * @return The message ID
   * @throws MEIncomingException
   *         if the message header does not contain an ebms message id
   */
  public static String getMessageId (final SOAPMessage soapMessage) throws MEIncomingException
  {
    try
    {
      return SoapXPathUtil.getSingleNodeTextContent (soapMessage.getSOAPHeader (), "//:MessageInfo/:MessageId");
    }
    catch (final SOAPException e)
    {
      throw new MEIncomingException ("Failed to find MessageId", e);
    }
  }
}
