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
package com.helger.dcng.webapi.as4;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MERoutingInformation;
import com.helger.dcng.api.rest.DCNGOutgoingMessage;
import com.helger.dcng.api.rest.DCNGPayload;
import com.helger.dcng.api.rest.DcngRestJAXB;
import com.helger.dcng.core.api.DcngApiHelper;
import com.helger.dcng.core.regrep.DcngRegRepHelperIt2;
import com.helger.dcng.webapi.ApiParamException;
import com.helger.dcng.webapi.helper.AbstractDcngApiInvoker;
import com.helger.dcng.webapi.helper.CommonApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.regrep.CRegRep4;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Send an outgoing AS4 message via the configured MEM gateway
 *
 * @author Philip Helger
 */
public class ApiPostSendIt2 extends AbstractDcngApiInvoker
{
  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException
  {
    // Read the payload as XML
    final DCNGOutgoingMessage aOutgoingMsg = DcngRestJAXB.outgoingMessage ().read (aRequestScope.getRequest ().getInputStream ());
    if (aOutgoingMsg == null)
      throw new ApiParamException ("Failed to interpret the message body as an 'OutgoingMessage'");
    if (aOutgoingMsg.getPayloadCount () != 1)
      throw new ApiParamException ("Exactly one 'OutgoingMessage/Payload' element is required");

    // These fields are optional in the XSD but required here
    if (StringHelper.hasNoText (aOutgoingMsg.getMetadata ().getEndpointURL ()))
      throw new ApiParamException ("The 'OutgoingMessage/Metadata/EndpointURL' element MUST be present and not empty");
    if (ArrayHelper.isEmpty (aOutgoingMsg.getMetadata ().getReceiverCertificate ()))
      throw new ApiParamException ("The 'OutgoingMessage/Metadata/ReceiverCertificate' element MUST be present and not empty");

    // Convert metadata
    final IMERoutingInformation aRoutingInfo;
    try
    {
      aRoutingInfo = MERoutingInformation.createForSending (aOutgoingMsg.getMetadata ());
    }
    catch (final CertificateException ex)
    {
      throw new ApiParamException ("Invalid routing information provided: " + ex.getMessage ());
    }

    // Add payloads
    final MEMessage.Builder aMessage = MEMessage.builder ();
    final DCNGPayload aPayload = aOutgoingMsg.getPayloadAtIndex (0);
    final byte [] aRegRepPayload = DcngRegRepHelperIt2.wrapInRegRep (true, aPayload.getValue ());

    // RegRep should be first
    aMessage.addPayload (MEPayload.builder ()
                                  .mimeType (CRegRep4.MIME_TYPE_EBRS_XML)
                                  .contentID (MEPayload.createRandomContentID ())
                                  .data (aRegRepPayload));
    DE4AKafkaClient.send (EErrorLevel.INFO, "Successfully added RegRep dummy");

    // Start response
    final LookupAndSendingResult ret = new LookupAndSendingResult (aRoutingInfo.getSenderID (),
                                                                   aRoutingInfo.getReceiverID (),
                                                                   aRoutingInfo.getDocumentTypeID (),
                                                                   aRoutingInfo.getProcessID (),
                                                                   aRoutingInfo.getTransportProtocol ());
    ret.setLookupSuccess (true);
    ret.setLookupEndpointURL (aRoutingInfo.getEndpointURL ());

    CommonApiInvoker.invoke (ret, () -> {
      // Main sending - throws Exception on error
      DcngApiHelper.sendAS4Message (aRoutingInfo, aMessage.build ());
      ret.setSendingSuccess (true);
      ret.setOverallSuccess (true);
    });

    return ret.getAsJson ();
  }
}
