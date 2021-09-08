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
import java.util.Map;

import javax.annotation.Nonnull;

import org.w3c.dom.Document;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.string.StringHelper;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.api.dd.IDDServiceMetadataProvider;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.me.model.MEPayload;
import com.helger.rdc.api.me.outgoing.MERoutingInformation;
import com.helger.rdc.api.me.outgoing.MERoutingInformationInput;
import com.helger.rdc.api.rest.RDCOutgoingMessage;
import com.helger.rdc.api.rest.RDCPayload;
import com.helger.rdc.api.rest.RdcRegRepHelper;
import com.helger.rdc.api.rest.RdcRestJAXB;
import com.helger.rdc.core.api.RdcApiHelper;
import com.helger.rdc.webapi.ApiParamException;
import com.helger.rdc.webapi.helper.AbstractRdcApiInvoker;
import com.helger.rdc.webapi.helper.CommonApiInvoker;
import com.helger.regrep.CRegRep4;
import com.helger.regrep.RegRep4Writer;
import com.helger.regrep.query.QueryRequest;
import com.helger.regrep.query.QueryResponse;
import com.helger.security.certificate.CertificateHelper;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xml.EXMLParserFeature;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;
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
    final IJsonObject aJson = new JsonObject ();
    {
      aJson.add ("senderid", aRoutingInfoBase.getSenderID ().getURIEncoded ());
      aJson.add ("receiverid", aRoutingInfoBase.getReceiverID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, aRoutingInfoBase.getDocumentTypeID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_PROCESS_ID, aRoutingInfoBase.getProcessID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_TRANSPORT_PROFILE, aRoutingInfoBase.getTransportProtocol ());
    }

    CommonApiInvoker.invoke (aJson, () -> {
      boolean bOverallSuccess = false;
      MERoutingInformation aRoutingInfo = null;

      // Query SMP
      {
        final IJsonObject aJsonSMP = new JsonObject ();
        // Main query
        final ServiceMetadataType aSM = RdcApiHelper.querySMPServiceMetadata (aRoutingInfoBase.getReceiverID (),
                                                                              aRoutingInfoBase.getDocumentTypeID (),
                                                                              aRoutingInfoBase.getProcessID (),
                                                                              aRoutingInfoBase.getTransportProtocol ());
        if (aSM != null)
        {
          aJsonSMP.addJson ("response",
                            SMPJsonResponse.convert (aRoutingInfoBase.getReceiverID (), aRoutingInfoBase.getDocumentTypeID (), aSM));

          final EndpointType aEndpoint = IDDServiceMetadataProvider.getEndpoint (aSM,
                                                                                 aRoutingInfoBase.getProcessID (),
                                                                                 aRoutingInfoBase.getTransportProtocol ());
          if (aEndpoint != null)
          {
            aJsonSMP.add (SMPJsonResponse.JSON_ENDPOINT_REFERENCE, aEndpoint.getEndpointURI ());
            aRoutingInfo = MERoutingInformation.create (aRoutingInfoBase,
                                                        aEndpoint.getEndpointURI (),
                                                        CertificateHelper.convertByteArrayToCertficateDirect (aEndpoint.getCertificate ()));
          }
          if (aRoutingInfo == null)
          {
            DE4AKafkaClient.send (EErrorLevel.WARN,
                                  () -> "[API] The SMP lookup for '" +
                                        aRoutingInfoBase.getReceiverID ().getURIEncoded () +
                                        "' and '" +
                                        aRoutingInfoBase.getDocumentTypeID ().getURIEncoded () +
                                        "' succeeded, but no endpoint matching '" +
                                        aRoutingInfoBase.getProcessID ().getURIEncoded () +
                                        "' and '" +
                                        aRoutingInfoBase.getTransportProtocol () +
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
        for (final RDCPayload aPayload : aOutgoingMsg.getPayload ())
        {
          if (nIndex == 0)
          {
            final Document aDoc = DOMReader.readXMLDOM (aPayload.getValue (),
                                                        new DOMReaderSettings ().setFeatureValues (EXMLParserFeature.AVOID_XML_ATTACKS));
            if (aDoc == null)
              throw new IllegalStateException ("Failed to parse first payload as XML");

            final byte [] aRegRepPayload;
            // TODO
            if (aPayload.getContentID ().contains ("Request"))
            {
              final QueryRequest aRRReq = RdcRegRepHelper.wrapInQueryRequest ("who", "cares", "person");
              aRegRepPayload = RegRep4Writer.queryRequest ().setFormattedOutput (true).getAsBytes (aRRReq);
            }
            else
            {
              final QueryResponse aRRResp = RdcRegRepHelper.wrapInQueryResponse ("no", "body");
              aRegRepPayload = RegRep4Writer.queryResponse ().setFormattedOutput (true).getAsBytes (aRRResp);
            }

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
}
