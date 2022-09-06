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
package com.helger.dcng.phase4;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.io.file.FileOperationManager;
import com.helger.commons.mime.EMimeContentType;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.commons.wrapper.Wrapper;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.me.IMessageExchangeSPI;
import com.helger.dcng.api.me.incoming.IMEIncomingHandler;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.dcng.core.http.DcngHttpClientSettings;
import com.helger.dcng.phase4.config.DcngPMode;
import com.helger.dcng.phase4.servlet.AS4MessageProcessorSPI;
import com.helger.peppol.utils.PeppolCertificateHelper;
import com.helger.phase4.attachment.EAS4CompressionMode;
import com.helger.phase4.attachment.Phase4OutgoingAttachment;
import com.helger.phase4.cef.Phase4CEFSender.CEFUserMessageBuilder;
import com.helger.phase4.crypto.IAS4CryptoFactory;
import com.helger.phase4.dump.AS4DumpManager;
import com.helger.phase4.dump.AS4IncomingDumperFileBased;
import com.helger.phase4.dump.AS4OutgoingDumperFileBased;
import com.helger.phase4.dump.AS4RawResponseConsumerWriteToFile;
import com.helger.phase4.dynamicdiscovery.AS4EndpointDetailProviderConstant;
import com.helger.phase4.http.AS4HttpDebug;
import com.helger.phase4.messaging.domain.MessageHelperMethods;
import com.helger.phase4.mgr.MetaAS4Manager;
import com.helger.phase4.model.pmode.IPModeManager;
import com.helger.phase4.model.pmode.PMode;
import com.helger.phase4.model.pmode.PModePayloadService;
import com.helger.phase4.sender.AbstractAS4UserMessageBuilder.ESimpleUserMessageSendResult;
import com.helger.phase4.servlet.AS4ServerInitializer;
import com.helger.photon.app.io.WebFileIO;
import com.helger.servlet.ServletHelper;

/**
 * DE4A {@link IMessageExchangeSPI} implementation using phase4.
 *
 * @author Philip Helger
 */
@IsSPIImplementation
public class Phase4MessageExchangeSPI implements IMessageExchangeSPI
{
  public static final String ID = "phase4";
  private static final Logger LOGGER = LoggerFactory.getLogger (Phase4MessageExchangeSPI.class);

  private IAS4CryptoFactory m_aCF;

  public Phase4MessageExchangeSPI ()
  {
    m_aCF = Phase4Config.getCryptoFactory ();
  }

  /**
   * @return The crypto factory in use. Never <code>null</code>.
   */
  @Nonnull
  public final IAS4CryptoFactory getCryptoFactory ()
  {
    return m_aCF;
  }

  /**
   * Change the crypto factory deduced from the configuration file. MUST be
   * called before {@link #init(ServletContext, IMEIncomingHandler)} to have an
   * effect!
   *
   * @param aCF
   *        The crypto factory to use. May not be <code>null</code>.
   */
  public final void setCryptoFactory (@Nonnull final IAS4CryptoFactory aCF)
  {
    ValueEnforcer.notNull (aCF, "CryptoFactory");
    m_aCF = aCF;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return ID;
  }

  @Nonnull
  @Nonempty
  private static final File _getTargetFolder (@Nonnull final String sPath)
  {
    final File ret = new File (sPath);
    FileOperationManager.INSTANCE.createDirRecursiveIfNotExisting (ret);
    return ret;
  }

  public void init (@Nonnull final ServletContext aServletContext, @Nonnull final IMEIncomingHandler aIncomingHandler)
  {
    ValueEnforcer.notNull (aServletContext, "ServletContext");
    ValueEnforcer.notNull (aIncomingHandler, "IncomingHandler");

    {
      // Check phase4 configuration
      final IAS4CryptoFactory aCF = Phase4Config.getCryptoFactory ();
      if (aCF == null)
        throw new InitializationException ("Failed to load the configured phase4 crypto configuration");
      if (aCF.getPrivateKeyEntry () == null)
        throw new InitializationException ("Failed to load the private key from the phase4 crypto configuration");
      if (aCF.getTrustStore () == null)
        throw new InitializationException ("Failed to load the trust store from the phase4 crypto configuration");
    }

    if (!WebFileIO.isInited ())
    {
      // Get the ServletContext base path
      final String sServletContextPath = ServletHelper.getServletContextBasePath (aServletContext);

      // Get the data path
      final String sDataPath = Phase4Config.getDataPath ();
      if (StringHelper.hasNoText (sDataPath))
        throw new InitializationException ("No phase4 data path was provided!");
      final File aDataPath = new File (sDataPath).getAbsoluteFile ();
      // Init the IO layer
      WebFileIO.initPaths (aDataPath, sServletContextPath, false);
    }

    // Sanity check
    {
      final KeyStore aOurKS = m_aCF.getKeyStore ();
      if (aOurKS == null)
        throw new InitializationException ("Failed to load configured phase4 keystore");

      final PrivateKeyEntry aOurKey = m_aCF.getPrivateKeyEntry ();
      if (aOurKey == null)
        throw new InitializationException ("Failed to load configured phase4 key");

      if (StringHelper.hasNoText (Phase4Config.getFromPartyID ()))
        throw new InitializationException ("The phase4 property 'phase4.send.fromparty.id' is missing or empty.");
      if (StringHelper.hasNoText (Phase4Config.getFromPartyIDType ()))
        throw new InitializationException ("The phase4 property 'phase4.send.fromparty.id.type' is missing or empty.");
      if (StringHelper.hasNoText (Phase4Config.getToPartyIDType ()))
        throw new InitializationException ("The phase4 property 'phase4.send.toparty.id.type' is missing or empty.");

      LOGGER.info ("Verified that the phase4 keystore configuration can be loaded");
    }

    // Register server once
    AS4ServerInitializer.initAS4Server ();

    final IPModeManager aPModeMgr = MetaAS4Manager.getPModeMgr ();
    {
      final PMode aPMode = DcngPMode.createDCNGMode ("AnyInitiatorID",
                                                     "AnyResponderID",
                                                     "AnyResponderAddress",
                                                     "DE4A_PMODE",
                                                     false);
      aPMode.setPayloadService (new PModePayloadService (EAS4CompressionMode.GZIP));
      aPMode.getReceptionAwareness ().setRetry (false);
      aPModeMgr.createOrUpdatePMode (aPMode);
    }

    // Remember handler
    AS4MessageProcessorSPI.setIncomingHandler (aIncomingHandler);

    // Enable debug (incoming and outgoing)
    AS4HttpDebug.setEnabled (Phase4Config.isHttpDebugEnabled ());

    // Set incoming dumper
    final String sIncomingDumpPath = Phase4Config.getDumpPathIncoming ();
    if (StringHelper.hasText (sIncomingDumpPath))
    {
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Dumping incoming phase4 AS4 messages to '" + sIncomingDumpPath + "'");
      AS4DumpManager.setIncomingDumper (new AS4IncomingDumperFileBased ( (aMessageMetadata,
                                                                          aHttpHeaderMap) -> new File (_getTargetFolder (sIncomingDumpPath),
                                                                                                       AS4IncomingDumperFileBased.IFileProvider.getFilename (aMessageMetadata))));
    }

    // Set outgoing dumper
    final String sOutgoingDumpPath = Phase4Config.getDumpPathOutgoing ();
    if (StringHelper.hasText (sOutgoingDumpPath))
    {
      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Dumping outgoing phase4 AS4 messages to '" + sOutgoingDumpPath + "'");
      AS4DumpManager.setOutgoingDumper (new AS4OutgoingDumperFileBased ( (eMsgMode,
                                                                          sMessageID,
                                                                          nTry) -> new File (_getTargetFolder (sOutgoingDumpPath),
                                                                                             AS4OutgoingDumperFileBased.IFileProvider.getFilename (sMessageID,
                                                                                                                                                   nTry))));
    }

    MessageHelperMethods.setCustomMessageIDSuffix ("de4a.dcng");
  }

  private static void _sendOutgoing (@Nonnull final IAS4CryptoFactory aCF,
                                     @Nonnull final IMERoutingInformation aRoutingInfo,
                                     @Nonnull final MEMessage aMessage) throws MEOutgoingException
  {
    final X509Certificate aTheirCert = aRoutingInfo.getCertificate ();

    final CEFUserMessageBuilder aBuilder = new CEFUserMessageBuilder ().httpClientFactory (new DcngHttpClientSettings ())
                                                                       .cryptoFactory (aCF)
                                                                       .senderParticipantID (aRoutingInfo.getSenderID ())
                                                                       .receiverParticipantID (aRoutingInfo.getReceiverID ())
                                                                       .documentTypeID (aRoutingInfo.getDocumentTypeID ())
                                                                       .processID (aRoutingInfo.getProcessID ())
                                                                       .conversationID (MessageHelperMethods.createRandomConversationID ())
                                                                       .fromPartyIDType (Phase4Config.getFromPartyIDType ())
                                                                       .fromPartyID (Phase4Config.getFromPartyID ())
                                                                       .fromRole (DcngPMode.PARTY_ROLE)
                                                                       .toPartyIDType (Phase4Config.getToPartyIDType ())
                                                                       .toPartyID (PeppolCertificateHelper.getCNOrNull (aTheirCert.getSubjectX500Principal ()
                                                                                                                                  .getName ()))
                                                                       .toRole (DcngPMode.PARTY_ROLE)
                                                                       .useOriginalSenderFinalRecipientTypeAttr (false)
                                                                       .rawResponseConsumer (new AS4RawResponseConsumerWriteToFile ())
                                                                       .endpointDetailProvider (new AS4EndpointDetailProviderConstant (aRoutingInfo.getCertificate (),
                                                                                                                                       aRoutingInfo.getEndpointURL ()));

    // Payload/attachments
    int nPayloadIndex = 0;
    for (final MEPayload aPayload : aMessage.payloads ())
    {
      // Compress only text
      final Phase4OutgoingAttachment aOA = Phase4OutgoingAttachment.builder ()
                                                                   .data (aPayload.getData ())
                                                                   .contentID (aPayload.getContentID ())
                                                                   .mimeType (aPayload.getMimeType ())
                                                                   .compression (aPayload.getMimeType ()
                                                                                         .getContentType () == EMimeContentType.TEXT ? EAS4CompressionMode.GZIP
                                                                                                                                     : null)
                                                                   .build ();
      if (nPayloadIndex == 0)
        aBuilder.payload (aOA);
      else
        aBuilder.addAttachment (aOA);
      nPayloadIndex++;
    }

    final Wrapper <Exception> aKeeper = new Wrapper <> ();
    final ESimpleUserMessageSendResult eRet = aBuilder.sendMessageAndCheckForReceipt (aKeeper::set);
    if (eRet.isSuccess ())
    {
      LOGGER.info ("[phase4] Sucessfully sent message");
    }
    else
    {
      if (LOGGER.isErrorEnabled ())
        LOGGER.error ("[phase4] Failed to send message: " + eRet);
      throw new MEOutgoingException (EDcngErrorCode.ME_001, aKeeper.get ());
    }
  }

  public void sendOutgoing (@Nonnull final IMERoutingInformation aRoutingInfo,
                            @Nonnull final MEMessage aMessage) throws MEOutgoingException
  {
    LOGGER.info ("[phase4] sendOutgoing");
    _sendOutgoing (m_aCF, aRoutingInfo, aMessage);
  }

  public void shutdown (@Nonnull final ServletContext aServletContext)
  {
    // Nothing to do here
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("CryptoFactory", m_aCF).getToString ();
  }
}
