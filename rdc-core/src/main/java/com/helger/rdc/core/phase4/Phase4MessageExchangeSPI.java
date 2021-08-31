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
package com.helger.rdc.core.phase4;

import java.io.File;
import java.security.KeyStore;
import java.security.KeyStore.PrivateKeyEntry;
import java.security.cert.X509Certificate;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.naming.InvalidNameException;
import javax.servlet.ServletContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.IsSPIImplementation;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.io.file.FileOperationManager;
import com.helger.commons.mime.EMimeContentType;
import com.helger.commons.string.StringHelper;
import com.helger.commons.string.ToStringGenerator;
import com.helger.peppol.utils.PeppolCertificateHelper;
import com.helger.phase4.attachment.EAS4CompressionMode;
import com.helger.phase4.attachment.Phase4OutgoingAttachment;
import com.helger.phase4.cef.Phase4CEFSender.CEFUserMessageBuilder;
import com.helger.phase4.crypto.IAS4CryptoFactory;
import com.helger.phase4.dump.AS4DumpManager;
import com.helger.phase4.dump.AS4IncomingDumperFileBased;
import com.helger.phase4.dump.AS4OutgoingDumperFileBased;
import com.helger.phase4.dynamicdiscovery.AS4EndpointDetailProviderConstant;
import com.helger.phase4.http.AS4HttpDebug;
import com.helger.phase4.messaging.domain.MessageHelperMethods;
import com.helger.phase4.mgr.MetaAS4Manager;
import com.helger.phase4.model.pmode.IPModeManager;
import com.helger.phase4.model.pmode.PMode;
import com.helger.phase4.model.pmode.PModePayloadService;
import com.helger.phase4.servlet.AS4ServerInitializer;
import com.helger.phase4.util.Phase4Exception;
import com.helger.photon.app.io.WebFileIO;
import com.helger.rdc.api.error.ERdcErrorCode;
import com.helger.rdc.api.http.RdcHttpClientSettings;
import com.helger.rdc.api.me.IMessageExchangeSPI;
import com.helger.rdc.api.me.incoming.IMEIncomingHandler;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.me.model.MEPayload;
import com.helger.rdc.api.me.outgoing.IMERoutingInformation;
import com.helger.rdc.api.me.outgoing.MEOutgoingException;
import com.helger.rdc.core.phase4.config.RdcPMode;
import com.helger.rdc.core.phase4.servlet.AS4MessageProcessorSPI;
import com.helger.servlet.ServletHelper;

/**
 * TOOP {@link IMessageExchangeSPI} implementation using phase4.
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
   * called before
   * {@link #registerIncomingHandler(ServletContext, IMEIncomingHandler)} to
   * have an effect!
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
    final LocalDate aLD = PDTFactory.getCurrentLocalDate ();
    final File ret = new File (sPath,
                               StringHelper.getLeadingZero (aLD.getYear (), 4) +
                                      "/" +
                                      StringHelper.getLeadingZero (aLD.getMonthValue (), 2) +
                                      "/" +
                                      StringHelper.getLeadingZero (aLD.getDayOfMonth (), 2));
    FileOperationManager.INSTANCE.createDirRecursiveIfNotExisting (ret);
    return ret;
  }

  public void registerIncomingHandler (@Nonnull final ServletContext aServletContext, @Nonnull final IMEIncomingHandler aIncomingHandler)
  {
    ValueEnforcer.notNull (aServletContext, "ServletContext");
    ValueEnforcer.notNull (aIncomingHandler, "IncomingHandler");

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

      LOGGER.info ("Verified that the phase4 keystore configuration can be loaded");
    }

    // Register server once
    AS4ServerInitializer.initAS4Server ();

    final IPModeManager aPModeMgr = MetaAS4Manager.getPModeMgr ();
    {
      final PMode aPMode = RdcPMode.createRDCMode ("AnyInitiatorID", "AnyResponderID", "AnyResponderAddress", "TOOP_PMODE", false);
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
      LOGGER.info ("Dumping incoming phase4 AS4 messages to '" + sIncomingDumpPath + "'");
      AS4DumpManager.setIncomingDumper (new AS4IncomingDumperFileBased ( (aMessageMetadata,
                                                                          aHttpHeaderMap) -> new File (_getTargetFolder (sIncomingDumpPath),
                                                                                                       AS4IncomingDumperFileBased.IFileProvider.getFilename (aMessageMetadata))));
    }

    // Set outgoing dumper
    final String sOutgoingDumpPath = Phase4Config.getDumpPathOutgoing ();
    if (StringHelper.hasText (sOutgoingDumpPath))
    {
      LOGGER.info ("Dumping outgoing phase4 AS4 messages to '" + sOutgoingDumpPath + "'");
      AS4DumpManager.setOutgoingDumper (new AS4OutgoingDumperFileBased ( (eMsgMode,
                                                                          sMessageID,
                                                                          nTry) -> new File (_getTargetFolder (sOutgoingDumpPath),
                                                                                             AS4OutgoingDumperFileBased.IFileProvider.getFilename (sMessageID,
                                                                                                                                                   nTry))));
    }
  }

  private static void _sendOutgoing (@Nonnull final IAS4CryptoFactory aCF,
                                     @Nonnull final IMERoutingInformation aRoutingInfo,
                                     @Nonnull final MEMessage aMessage) throws MEOutgoingException
  {
    try
    {
      final X509Certificate aTheirCert = aRoutingInfo.getCertificate ();

      // See :
      // http://wiki.ds.unipi.gr/display/TOOP/Routing+Information+Profile
      // http://wiki.ds.unipi.gr/display/CCTF/TOOP+AS4+GW+Interface+specification
      final CEFUserMessageBuilder aBuilder = new CEFUserMessageBuilder ().httpClientFactory (new RdcHttpClientSettings ())
                                                                         .cryptoFactory (aCF)
                                                                         .senderParticipantID (aRoutingInfo.getSenderID ())
                                                                         .receiverParticipantID (aRoutingInfo.getReceiverID ())
                                                                         .documentTypeID (aRoutingInfo.getDocumentTypeID ())
                                                                         .processID (aRoutingInfo.getProcessID ())
                                                                         .conversationID (MessageHelperMethods.createRandomConversationID ())
                                                                         .fromPartyIDType (Phase4Config.getFromPartyIDType ())
                                                                         .fromPartyID (Phase4Config.getFromPartyID ())
                                                                         .fromRole ("http://www.toop.eu/edelivery/gateway")
                                                                         .toPartyIDType (Phase4Config.getToPartyIDType ())
                                                                         .toPartyID (PeppolCertificateHelper.getCN (aTheirCert.getSubjectX500Principal ()
                                                                                                                              .getName ()))
                                                                         .toRole ("http://www.toop.eu/edelivery/gateway")
                                                                         .useOriginalSenderFinalRecipientTypeAttr (false)
                                                                         .rawResponseConsumer (new RawResponseWriter ())
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

      if (aBuilder.sendMessage ().isSuccess ())
      {
        LOGGER.info ("[phase4] Sucessfully sent message");
      }
      else
      {
        LOGGER.error ("[phase4] Failed to send message");
      }
    }
    catch (final Phase4Exception | InvalidNameException ex)
    {
      LOGGER.error ("[phase4] Error sending message", ex);
      throw new MEOutgoingException (ERdcErrorCode.ME_001, ex);
    }
  }

  public void sendOutgoing (@Nonnull final IMERoutingInformation aRoutingInfo, @Nonnull final MEMessage aMessage) throws MEOutgoingException
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
