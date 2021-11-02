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
package com.helger.dcng.webapi.as4;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.dd.IDDServiceMetadataProvider;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.MERoutingInformation;
import com.helger.dcng.api.me.outgoing.MERoutingInformationInput;
import com.helger.dcng.api.rest.RdcRestJAXB;
import com.helger.dcng.core.api.RdcApiHelper;
import com.helger.dcng.core.regrep.RdcRegRepHelper;
import com.helger.dcng.webapi.ApiParamException;
import com.helger.dcng.webapi.helper.AbstractRdcApiInvoker;
import com.helger.dcng.webapi.helper.CommonApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.api.rest.RDCOutgoingMessage;
import com.helger.rdc.api.rest.RDCPayload;
import com.helger.regrep.CRegRep4;
import com.helger.security.certificate.CertificateHelper;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Perform validation, lookup and sending via API
 *
 * @author Philip Helger
 */
public class ApiPostLookendAndSend extends AbstractRdcApiInvoker
{
  public ApiPostLookendAndSend ()
  {}

  @Nonnull
  public static IJsonObject perform (@Nonnull final IParticipantIdentifier aSenderID,
                                     @Nonnull final IParticipantIdentifier aReceiverID,
                                     @Nonnull final IDocumentTypeIdentifier aDocumentTypeID,
                                     @Nonnull final IProcessIdentifier aProcessID,
                                     @Nonnull final String sTransportProfile,
                                     @Nonnull final Iterable <RDCPayload> aPayloads)
  {
    // Start response
    final IJsonObject aJson = new JsonObject ();
    {
      aJson.add ("senderid", aSenderID.getURIEncoded ());
      aJson.add ("receiverid", aReceiverID.getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, aDocumentTypeID.getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_PROCESS_ID, aProcessID.getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_TRANSPORT_PROFILE, sTransportProfile);
    }

    CommonApiInvoker.invoke (aJson, () -> {
      boolean bOverallSuccess = false;
      MERoutingInformation aRoutingInfo = null;

      // Query SMP
      {
        final IJsonObject aJsonSMP = new JsonObject ();
        // Main query
        final ServiceMetadataType aSM = RdcApiHelper.querySMPServiceMetadata (aReceiverID, aDocumentTypeID, aProcessID, sTransportProfile);
        if (aSM != null)
        {
          aJsonSMP.addJson ("response", SMPJsonResponse.convert (aReceiverID, aDocumentTypeID, aSM));

          final EndpointType aEndpoint = IDDServiceMetadataProvider.getEndpoint (aSM, aProcessID, sTransportProfile);
          if (aEndpoint != null)
          {
            aJsonSMP.add (SMPJsonResponse.JSON_ENDPOINT_REFERENCE, aEndpoint.getEndpointURI ());
            aRoutingInfo = new MERoutingInformation (aSenderID,
                                                     aReceiverID,
                                                     aDocumentTypeID,
                                                     aProcessID,
                                                     sTransportProfile,
                                                     aEndpoint.getEndpointURI (),
                                                     CertificateHelper.convertByteArrayToCertficateDirect (aEndpoint.getCertificate ()));
          }
          if (aRoutingInfo == null)
          {
            DE4AKafkaClient.send (EErrorLevel.WARN,
                                  () -> "[API] The SMP lookup for '" +
                                        aReceiverID.getURIEncoded () +
                                        "' and '" +
                                        aDocumentTypeID.getURIEncoded () +
                                        "' succeeded, but no endpoint matching '" +
                                        aProcessID.getURIEncoded () +
                                        "' and '" +
                                        sTransportProfile +
                                        "' was found.");
          }

          // Only if a match was found
          aJsonSMP.add (JSON_SUCCESS, aRoutingInfo != null);
        }
        else
          aJsonSMP.add (JSON_SUCCESS, false);
        aJson.addJson ("lookup-results", aJsonSMP);
      }

      // Read for AS4 sending?
      if (aRoutingInfo != null)
      {
        final IJsonObject aJsonSending = new JsonObject ();

        // Add payloads
        final MEMessage.Builder aMessage = MEMessage.builder ();
        int nIndex = 0;
        for (final RDCPayload aPayload : aPayloads)
        {
          if (nIndex == 0)
          {
            final byte [] aRegRepPayload = RdcRegRepHelper.wrapInRegRep (aPayload.getContentID (), aPayload.getValue ());

            // RegRep should be first
            aMessage.addPayload (MEPayload.builder ()
                                          .mimeType (CRegRep4.MIME_TYPE_EBRS_XML)
                                          .contentID (MEPayload.createRandomContentID ())
                                          .data (aRegRepPayload));
          }

          aMessage.addPayload (MEPayload.builder ()
                                        .mimeType (MimeTypeParser.parseMimeType (aPayload.getMimeType ()))
                                        .contentID (StringHelper.getNotEmpty (aPayload.getContentID (), MEPayload.createRandomContentID ()))
                                        .data (aPayload.getValue ()));
          nIndex++;
        }
        RdcApiHelper.sendAS4Message (aRoutingInfo, aMessage.build ());
        aJsonSending.add (JSON_SUCCESS, true);

        aJson.addJson ("sending-results", aJsonSending);
        bOverallSuccess = true;
      }

      // Overall success
      aJson.add (JSON_SUCCESS, bOverallSuccess);
    });

    return aJson;
  }

  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException
  {
    // Read the payload as XML
    final RDCOutgoingMessage aOutgoingMsg = RdcRestJAXB.outgoingMessage ().read (aRequestScope.getRequest ().getInputStream ());
    if (aOutgoingMsg == null)
      throw new ApiParamException ("Failed to interpret the message body as an 'OutgoingMessage'");

    // These fields MUST not be present here - they are filled while we go
    if (StringHelper.hasText (aOutgoingMsg.getMetadata ().getEndpointURL ()))
      throw new ApiParamException ("The 'OutgoingMessage/Metadata/EndpointURL' element MUST NOT be present");
    if (ArrayHelper.isNotEmpty (aOutgoingMsg.getMetadata ().getReceiverCertificate ()))
      throw new ApiParamException ("The 'OutgoingMessage/Metadata/ReceiverCertificate' element MUST NOT be present");

    // Convert metadata
    final MERoutingInformationInput aRoutingInfoBase = MERoutingInformationInput.createBaseForSending (aOutgoingMsg.getMetadata ());

    // Start response
    return perform (aRoutingInfoBase.getSenderID (),
                    aRoutingInfoBase.getReceiverID (),
                    aRoutingInfoBase.getDocumentTypeID (),
                    aRoutingInfoBase.getProcessID (),
                    aRoutingInfoBase.getTransportProtocol (),
                    aOutgoingMsg.getPayload ());
  }
}
