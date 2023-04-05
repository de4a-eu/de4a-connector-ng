/*
 * Copyright (C) 2023, Partners of the EU funded DE4A project consortium
 *   (https://www.de4a.eu/consortium), under Grant Agreement No.870635
 * Author: Austrian Federal Computing Center (BRZ)
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.base64.Base64;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.id.factory.GlobalIDFactory;
import com.helger.commons.io.file.FileHelper;
import com.helger.commons.io.file.FileOperationManager;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTIOHelper;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.json.IJsonArray;
import com.helger.json.IJsonObject;
import com.helger.json.JsonArray;
import com.helger.json.JsonObject;
import com.helger.json.serialize.JsonWriter;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.security.certificate.CertificateHelper;

/**
 * Dumper for MEM/External messages. Must be enabled via the configuration.
 *
 * @author Philip Helger
 * @since 2.0.0-rc3
 */
@Immutable
public final class MEMDumper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MEMDumper.class);

  private MEMDumper ()
  {}

  @Nonnull
  private static IJsonObject _asJson (@Nonnull final MEMessage aMessage)
  {
    final IJsonObject ret = new JsonObject ();
    if (aMessage.getSenderID () != null)
      ret.add ("sender", aMessage.getSenderID ().getURIEncoded ());
    if (aMessage.getReceiverID () != null)
      ret.add ("receiver", aMessage.getReceiverID ().getURIEncoded ());
    if (aMessage.getDocumentTypeID () != null)
      ret.add ("doctype", aMessage.getDocumentTypeID ().getURIEncoded ());
    if (aMessage.getProcessID () != null)
      ret.add ("process", aMessage.getProcessID ().getURIEncoded ());
    final IJsonArray aJsonPayloads = new JsonArray ();
    for (final MEPayload aPayload : aMessage.payloads ())
    {
      aJsonPayloads.add (new JsonObject ().add ("mimeType", aPayload.getMimeTypeString ())
                                          .add ("contentID", aPayload.getContentID ())
                                          .add ("data", Base64.safeEncodeBytes (aPayload.getData ().bytes ())));
    }
    ret.addJson ("payloads", aJsonPayloads);
    return ret;
  }

  @Nonnull
  @Nonempty
  private static File _getTargetFolder (@Nonnull final String sPath)
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

  @Nonnull
  @Nonempty
  private static String _getFileID ()
  {
    return PDTIOHelper.getCurrentLocalDateTimeForFilename () + "-" + GlobalIDFactory.getNewIntID ();
  }

  /**
   * Dump an outgoing message if dumping is enabled.
   *
   * @param aRoutingInfo
   *        Routing information. May not be <code>null</code>.
   * @param aMessage
   *        The message to be exchanged. May not be <code>null</code>.
   */
  public static void dumpOutgoingMessage (@Nonnull final IMERoutingInformation aRoutingInfo, @Nonnull final MEMessage aMessage)
  {
    if (MEMHolodeckConfig.isMEMOutgoingDumpEnabled ())
    {
      final String sPath = MEMHolodeckConfig.getMEMOutgoingDumpPath ();
      if (StringHelper.hasText (sPath))
      {
        final IJsonObject aJson = new JsonObject ().addJson ("routing",
                                                             new JsonObject ().add ("sender", aRoutingInfo.getSenderID ().getURIEncoded ())
                                                                              .add ("receiver",
                                                                                    aRoutingInfo.getReceiverID ().getURIEncoded ())
                                                                              .add ("doctype",
                                                                                    aRoutingInfo.getDocumentTypeID ().getURIEncoded ())
                                                                              .add ("process",
                                                                                    aRoutingInfo.getProcessID ().getURIEncoded ())
                                                                              .add ("transportProtocol",
                                                                                    aRoutingInfo.getTransportProtocol ())

                                                                              .add ("endpointURL", aRoutingInfo.getEndpointURL ())
                                                                              .add ("certificate",
                                                                                    CertificateHelper.getPEMEncodedCertificate (aRoutingInfo.getCertificate ())))
                                                   .addJson ("message", _asJson (aMessage));

        final File aTargetFile = new File (_getTargetFolder (sPath), "toop-mem-external-outgoing-" + _getFileID () + ".json");
        try (final OutputStream aOS = FileHelper.getBufferedOutputStream (aTargetFile))
        {
          new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeToStream (aJson, aOS, StandardCharsets.UTF_8);
          LOGGER.info ("Wrote outgoing MEM dump file '" + aTargetFile.getAbsolutePath () + "'");
        }
        catch (final IOException ex)
        {
          LOGGER.error ("Error writing to outgoing MEM dump file '" + aTargetFile.getAbsolutePath () + "'", ex);
        }
      }
      else
        LOGGER.warn ("Dumping of outgoing MEM messages is enabled, but no dump path was configured. Not dumping the message.");
    }
  }

  public static void dumpOutgoingMessage (@Nonnull final SOAPMessage aMessage)
  {
    if (MEMHolodeckConfig.isMEMOutgoingDumpEnabled ())
    {
      final String sPath = MEMHolodeckConfig.getMEMOutgoingDumpPath ();
      if (StringHelper.hasText (sPath))
      {
        try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
        {
          aMessage.writeTo (aBAOS);

          final File aTargetFile = new File (_getTargetFolder (sPath), "toop-mem-external-outgoing-" + _getFileID () + ".raw");
          if (SimpleFileIO.writeFile (aTargetFile, aBAOS.toByteArray ()).isSuccess ())
            LOGGER.info ("Wrote outgoing MEM dump file '" + aTargetFile.getAbsolutePath () + "'");
          else
            LOGGER.error ("Error writing to outgoing MEM dump file '" + aTargetFile.getAbsolutePath () + "'");
        }
        catch (final SOAPException | IOException ex)
        {
          LOGGER.error ("Error dumping outgoing SOAP message", ex);
        }
      }
      else
        LOGGER.warn ("Dumping of outgoing MEM messages is enabled, but no dump path was configured. Not dumping the message.");
    }
  }

  public static void dumpIncomingMessage (@Nonnull final byte [] aBytes)
  {
    if (MEMHolodeckConfig.isMEMIncomingDumpEnabled ())
    {
      final String sPath = MEMHolodeckConfig.getMEMIncomingDumpPath ();
      if (StringHelper.hasText (sPath))
      {
        final String sFilename = "toop-mem-external-incoming-" + _getFileID () + ".raw";
        final File aTargetFile = new File (_getTargetFolder (sPath), sFilename);
        if (SimpleFileIO.writeFile (aTargetFile, aBytes).isSuccess ())
          LOGGER.info ("Wrote incoming MEM dump file '" + aTargetFile.getAbsolutePath () + "'");
        else
          LOGGER.error ("Error writing to incoming MEM dump file '" + aTargetFile.getAbsolutePath () + "'");
      }
      else
        LOGGER.warn ("Dumping of incoming MEM messages is enabled, but no dump path was configured. Not dumping the message.");
    }
  }
}
