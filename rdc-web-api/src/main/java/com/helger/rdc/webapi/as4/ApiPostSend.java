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
package com.helger.rdc.webapi.as4;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.string.StringHelper;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.me.model.MEPayload;
import com.helger.rdc.api.me.outgoing.IMERoutingInformation;
import com.helger.rdc.api.me.outgoing.MERoutingInformation;
import com.helger.rdc.api.rest.TCOutgoingMessage;
import com.helger.rdc.api.rest.TCPayload;
import com.helger.rdc.api.rest.RdcRestJAXB;
import com.helger.rdc.core.api.RDCAPIHelper;
import com.helger.rdc.webapi.APIParamException;
import com.helger.rdc.webapi.helper.AbstractRDCAPIInvoker;
import com.helger.rdc.webapi.helper.CommonAPIInvoker;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * Send an outgoing AS4 message via the configured MEM gateway
 *
 * @author Philip Helger
 */
public class ApiPostSend extends AbstractRDCAPIInvoker
{
  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException
  {
    // Read the payload as XML
    final TCOutgoingMessage aOutgoingMsg = RdcRestJAXB.outgoingMessage ()
                                                     .read (aRequestScope.getRequest ().getInputStream ());
    if (aOutgoingMsg == null)
      throw new APIParamException ("Failed to interpret the message body as an 'OutgoingMessage'");

    // These fields are optional in the XSD but required here
    if (StringHelper.hasNoText (aOutgoingMsg.getMetadata ().getEndpointURL ()))
      throw new APIParamException ("The 'OutgoingMessage/Metadata/EndpointURL' element MUST be present and not empty");
    if (ArrayHelper.isEmpty (aOutgoingMsg.getMetadata ().getReceiverCertificate ()))
      throw new APIParamException ("The 'OutgoingMessage/Metadata/ReceiverCertificate' element MUST be present and not empty");

    // Convert metadata
    final IMERoutingInformation aRoutingInfo;
    try
    {
      aRoutingInfo = MERoutingInformation.createFrom (aOutgoingMsg.getMetadata ());
    }
    catch (final CertificateException ex)
    {
      throw new APIParamException ("Invalid routing information provided: " + ex.getMessage ());
    }

    // Add payloads
    final MEMessage.Builder aMessage = MEMessage.builder ();
    for (final TCPayload aPayload : aOutgoingMsg.getPayload ())
    {
      aMessage.addPayload (MEPayload.builder ()
                                    .mimeType (MimeTypeParser.safeParseMimeType (aPayload.getMimeType ()))
                                    .contentID (StringHelper.getNotEmpty (aPayload.getContentID (),
                                                                          MEPayload.createRandomContentID ()))
                                    .data (aPayload.getValue ()));
    }

    // Start response
    final IJsonObject aJson = new JsonObject ();
    {
      aJson.add ("senderid", aRoutingInfo.getSenderID ().getURIEncoded ());
      aJson.add ("receiverid", aRoutingInfo.getReceiverID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, aRoutingInfo.getDocumentTypeID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_PROCESS_ID, aRoutingInfo.getProcessID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_TRANSPORT_PROFILE, aRoutingInfo.getTransportProtocol ());
      aJson.add (SMPJsonResponse.JSON_ENDPOINT_REFERENCE, aRoutingInfo.getEndpointURL ());
    }

    CommonAPIInvoker.invoke (aJson, () -> {
      // Main sending - throws Exception on error
      RDCAPIHelper.sendAS4Message (aRoutingInfo, aMessage.build ());
      aJson.add (JSON_SUCCESS, true);
    });

    return aJson;
  }
}
