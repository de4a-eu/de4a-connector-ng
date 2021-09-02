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
package com.helger.rdc.core.incoming;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.mime.IMimeType;
import com.helger.commons.state.ESuccess;
import com.helger.commons.string.StringHelper;
import com.helger.commons.url.IURLProtocol;
import com.helger.commons.url.URLProtocolRegistry;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.api.http.RdcHttpClientSettings;
import com.helger.rdc.api.me.incoming.IMEIncomingTransportMetadata;
import com.helger.rdc.api.me.incoming.IncomingEDMRequest;
import com.helger.rdc.api.me.incoming.IncomingEDMResponse;
import com.helger.rdc.api.me.model.MEPayload;
import com.helger.rdc.api.rest.RDCIncomingMessage;
import com.helger.rdc.api.rest.RDCIncomingMetadata;
import com.helger.rdc.api.rest.RDCPayload;
import com.helger.rdc.api.rest.RDCPayloadType;
import com.helger.rdc.api.rest.RdcRestJAXB;
import com.helger.regrep.CRegRep4;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Push incoming messages to DC/DP via the HTTP interface.
 *
 * @author Philip Helger
 */
@Immutable
public final class RdcDPTriggerViaHttp
{
  private RdcDPTriggerViaHttp ()
  {}

  @Nonnull
  private static ESuccess _forwardMessage (@Nonnull final RDCIncomingMessage aMsg, @Nonnull @Nonempty final String sDestURL)
  {
    ValueEnforcer.notNull (aMsg, "Msg");
    ValueEnforcer.notEmpty (sDestURL, "Destination URL");

    if (StringHelper.hasNoText (sDestURL))
      throw new IllegalStateException ("No URL for handling inbound messages is defined.");

    final IURLProtocol aProtocol = URLProtocolRegistry.getInstance ().getProtocol (sDestURL);
    if (aProtocol == null)
      throw new IllegalStateException ("The URL for handling inbound messages '" + sDestURL + "' is invalid.");

    // Convert XML to bytes
    final byte [] aPayload = RdcRestJAXB.incomingMessage ().getAsBytes (aMsg);
    if (aPayload == null)
      throw new IllegalStateException ();

    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "Sending inbound message to '" + sDestURL + "' with " + aPayload.length + " bytes");

    // Main sending, using RDC http settings
    try (final HttpClientManager aHCM = HttpClientManager.create (new RdcHttpClientSettings ()))
    {
      final HttpPost aPost = new HttpPost (sDestURL);
      aPost.setEntity (new ByteArrayEntity (aPayload));
      final byte [] aResult = aHCM.execute (aPost, new ResponseHandlerByteArray ());

      DE4AKafkaClient.send (EErrorLevel.INFO,
                            () -> "Sending inbound message was successful. Got " + ArrayHelper.getSize (aResult) + " bytes back");
      return ESuccess.SUCCESS;
    }
    catch (final Exception ex)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Sending inbound message to '" + sDestURL + "' failed", ex);
      return ESuccess.FAILURE;
    }
  }

  @Nonnull
  private static RDCIncomingMetadata _createMetadata (@Nonnull final IMEIncomingTransportMetadata aMD,
                                                      @Nonnull final RDCPayloadType ePayloadType)
  {
    final RDCIncomingMetadata ret = new RDCIncomingMetadata ();
    ret.setSenderID (RdcRestJAXB.createRDCID (aMD.getSenderID ()));
    ret.setReceiverID (RdcRestJAXB.createRDCID (aMD.getReceiverID ()));
    ret.setDocTypeID (RdcRestJAXB.createRDCID (aMD.getDocumentTypeID ()));
    ret.setProcessID (RdcRestJAXB.createRDCID (aMD.getProcessID ()));
    ret.setPayloadType (ePayloadType);
    return ret;
  }

  @Nonnull
  private static RDCPayload _createPayload (@Nonnull final byte [] aValue,
                                            @Nullable final String sContentID,
                                            @Nonnull final IMimeType aMimeType)
  {
    final RDCPayload ret = new RDCPayload ();
    ret.setValue (aValue);
    ret.setContentID (sContentID);
    ret.setMimeType (aMimeType.getAsString ());
    return ret;
  }

  @Nonnull
  @Nonempty
  private static String _getConfiguredDestURL ()
  {
    final String ret = RdcConfig.MEM.getMEMIncomingURL ();
    if (StringHelper.hasNoText (ret))
      throw new IllegalStateException ("The MEM incoming URL for forwarding to DC/DP is not configured.");
    return ret;
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final IncomingEDMRequest aRequest)
  {
    return forwardMessage (aRequest, _getConfiguredDestURL ());
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final IncomingEDMRequest aRequest, @Nonnull @Nonempty final String sDestURL)
  {
    ValueEnforcer.notEmpty (sDestURL, "Destination URL");

    final RDCIncomingMessage aMsg = new RDCIncomingMessage ();
    aMsg.setMetadata (_createMetadata (aRequest.getMetadata (), RDCPayloadType.REQUEST));
    aMsg.addPayload (_createPayload (aRequest.getRequest ().getWriter ().getAsBytes (),
                                     aRequest.getTopLevelContentID (),
                                     CRegRep4.MIME_TYPE_EBRS_XML));
    return _forwardMessage (aMsg, sDestURL);
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final IncomingEDMResponse aResponse)
  {
    return forwardMessage (aResponse, _getConfiguredDestURL ());
  }

  @Nonnull
  public static ESuccess forwardMessage (@Nonnull final IncomingEDMResponse aResponse, @Nonnull @Nonempty final String sDestURL)
  {
    ValueEnforcer.notEmpty (sDestURL, "Destination URL");

    final RDCIncomingMessage aMsg = new RDCIncomingMessage ();
    aMsg.setMetadata (_createMetadata (aResponse.getMetadata (), RDCPayloadType.RESPONSE));
    aMsg.addPayload (_createPayload (aResponse.getResponse ().getWriter ().getAsBytes (),
                                     aResponse.getTopLevelContentID (),
                                     CRegRep4.MIME_TYPE_EBRS_XML));
    // Add all attachments
    for (final MEPayload aPayload : aResponse.attachments ().values ())
      aMsg.addPayload (_createPayload (aPayload.getData ().bytes (), aPayload.getContentID (), aPayload.getMimeType ()));
    return _forwardMessage (aMsg, sDestURL);
  }
}
