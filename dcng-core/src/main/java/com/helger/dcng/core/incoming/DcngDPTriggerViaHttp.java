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
package com.helger.dcng.core.incoming;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.state.ESuccess;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.IURLProtocol;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.rest.DCNGIncomingMessage;
import com.helger.dcng.api.rest.DCNGIncomingMetadata;
import com.helger.dcng.api.rest.DCNGPayload;
import com.helger.dcng.api.rest.DcngRestJAXB;
import com.helger.dcng.core.http.DcngHttpClientSettings;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.xml.serialize.write.XMLWriterSettings;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Push incoming messages to DC/DP via the HTTP interface.
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngDPTriggerViaHttp
{
  private DcngDPTriggerViaHttp ()
  {}

  @Nonnull
  private static ESuccess _forwardMessage (@Nonnull final DCNGIncomingMessage aMsg,
                                           @Nonnull @Nonempty final String sDestURL)
  {
    ValueEnforcer.notNull (aMsg, "Msg");
    ValueEnforcer.notEmpty (sDestURL, "Destination URL");

    if (StringHelper.hasNoText (sDestURL))
      throw new IllegalStateException ("No URL for handling inbound messages is defined.");

    final IURLProtocol aProtocol = URLProtocolRegistry.getInstance ().getProtocol (sDestURL);
    if (aProtocol == null)
      throw new IllegalStateException ("The URL for handling inbound messages '" + sDestURL + "' is invalid.");

    // Convert XML to bytes
    final byte [] aPayload = DcngRestJAXB.incomingMessage ().getAsBytes (aMsg);
    if (aPayload == null)
      throw new IllegalStateException ();

    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "Sending inbound message to '" + sDestURL + "' with " + aPayload.length + " bytes");

    // Main sending, using DCNG http settings
    try (final HttpClientManager aHCM = HttpClientManager.create (new DcngHttpClientSettings ()))
    {
      final HttpPost aPost = new HttpPost (sDestURL);
      aPost.setEntity (new ByteArrayEntity (aPayload,
                                            ContentType.APPLICATION_XML.withCharset (XMLWriterSettings.DEFAULT_XML_CHARSET_OBJ)));
      final byte [] aResult = aHCM.execute (aPost, new ResponseHandlerByteArray ());

      DE4AKafkaClient.send (EErrorLevel.INFO,
                            () -> "Sending inbound message was successful. Got " +
                                  ArrayHelper.getSize (aResult) +
                                  " bytes back");
      return ESuccess.SUCCESS;
    }
    catch (final Exception ex)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Sending inbound message to '" + sDestURL + "' failed", ex);
      return ESuccess.FAILURE;
    }
  }

  @Nonnull
  private static DCNGIncomingMetadata _createMetadata (@Nonnull final MEMessage aRequest)
  {
    final DCNGIncomingMetadata ret = new DCNGIncomingMetadata ();
    ret.setSenderID (DcngRestJAXB.createDCNGID (aRequest.getSenderID ()));
    ret.setReceiverID (DcngRestJAXB.createDCNGID (aRequest.getReceiverID ()));
    ret.setDocTypeID (DcngRestJAXB.createDCNGID (aRequest.getDocumentTypeID ()));
    ret.setProcessID (DcngRestJAXB.createDCNGID (aRequest.getProcessID ()));
    return ret;
  }

  @Nonnull
  private static DCNGPayload _createPayload (@Nonnull final byte [] aValue,
                                             @Nullable final String sContentID,
                                             @Nonnull final IMimeType aMimeType)
  {
    final DCNGPayload ret = new DCNGPayload ();
    ret.setValue (aValue);
    ret.setContentID (sContentID);
    ret.setMimeType (aMimeType.getAsString ());
    return ret;
  }

  @Nonnull
  @Nonempty
  private static String _getConfiguredDestURL ()
  {
    final String ret = DcngConfig.ME.getMEMIncomingURL ();
    if (StringHelper.hasNoText (ret))
      throw new IllegalStateException ("The MEM incoming URL for forwarding to DC/DP is not configured.");
    return ret;
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final MEMessage aRequest)
  {
    return forwardMessage (aRequest, _getConfiguredDestURL ());
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final MEMessage aRequest, @Nonnull @Nonempty final String sDestURL)
  {
    ValueEnforcer.notEmpty (sDestURL, "Destination URL");

    // Convert from MEMessage to DCNGIncomingMessage
    final DCNGIncomingMessage aMsg = new DCNGIncomingMessage ();
    aMsg.setMetadata (_createMetadata (aRequest));
    for (final MEPayload aPayload : aRequest.payloads ())
      aMsg.addPayload (_createPayload (aPayload.getData ().bytes (),
                                       aPayload.getContentID (),
                                       aPayload.getMimeType ()));
    return _forwardMessage (aMsg, sDestURL);
  }
}
