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
package com.helger.rdc.webapi.user;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.mime.MimeTypeParser;
import com.helger.commons.string.StringHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.phive.api.executorset.VESID;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.phive.json.PhiveJsonHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.api.dd.IDDServiceMetadataProvider;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.me.model.MEPayload;
import com.helger.rdc.api.me.outgoing.MERoutingInformation;
import com.helger.rdc.api.me.outgoing.MERoutingInformationInput;
import com.helger.rdc.api.rest.TCOutgoingMessage;
import com.helger.rdc.api.rest.TCPayload;
import com.helger.rdc.api.rest.RDCRestJAXB;
import com.helger.rdc.core.api.TCAPIHelper;
import com.helger.rdc.core.validation.TCValidator;
import com.helger.rdc.webapi.APIParamException;
import com.helger.rdc.webapi.ETCEdmType;
import com.helger.rdc.webapi.helper.AbstractTCAPIInvoker;
import com.helger.rdc.webapi.helper.CommonAPIInvoker;
import com.helger.security.certificate.CertificateHelper;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

/**
 * Perform validation, lookup and sending via API
 *
 * @author Philip Helger
 */
public class ApiPostUserSubmitEdm extends AbstractTCAPIInvoker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ApiPostUserSubmitEdm.class);

  private final ETCEdmType m_eType;

  public ApiPostUserSubmitEdm (@Nonnull final ETCEdmType eType)
  {
    m_eType = eType;
  }

  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException
  {
    // Read the payload as XML
    final TCOutgoingMessage aOutgoingMsg = RDCRestJAXB.outgoingMessage ()
                                                     .read (aRequestScope.getRequest ().getInputStream ());
    if (aOutgoingMsg == null)
      throw new APIParamException ("Failed to interpret the message body as an 'OutgoingMessage'");

    // These fields MUST not be present here - they are filled while we go
    if (StringHelper.hasText (aOutgoingMsg.getMetadata ().getEndpointURL ()))
      throw new APIParamException ("The 'OutgoingMessage/Metadata/EndpointURL' element MUST NOT be present");
    if (ArrayHelper.isNotEmpty (aOutgoingMsg.getMetadata ().getReceiverCertificate ()))
      throw new APIParamException ("The 'OutgoingMessage/Metadata/ReceiverCertificate' element MUST NOT be present");

    // Convert metadata
    final MERoutingInformationInput aRoutingInfo = MERoutingInformationInput.createForInput (aOutgoingMsg.getMetadata ());

    // Start response
    final IJsonObject aJson = new JsonObject ();
    {
      aJson.add ("senderid", aRoutingInfo.getSenderID ().getURIEncoded ());
      aJson.add ("receiverid", aRoutingInfo.getReceiverID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, aRoutingInfo.getDocumentTypeID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_PROCESS_ID, aRoutingInfo.getProcessID ().getURIEncoded ());
      aJson.add (SMPJsonResponse.JSON_TRANSPORT_PROFILE, aRoutingInfo.getTransportProtocol ());
    }

    CommonAPIInvoker.invoke (aJson, () -> {
      final boolean bValidationOK;
      boolean bOverallSuccess = false;
      {
        // validation
        final StopWatch aSW = StopWatch.createdStarted ();
        final VESID aVESID = m_eType.getVESID ();
        final ValidationResultList aValidationResultList = TCAPIHelper.validateBusinessDocument (aVESID,
                                                                                                 aOutgoingMsg.getPayloadAtIndex (0)
                                                                                                             .getValue ());
        aSW.stop ();

        final IJsonObject aJsonVR = new JsonObject ();
        PhiveJsonHelper.applyValidationResultList (aJsonVR,
                                                   TCValidator.getVES (aVESID),
                                                   aValidationResultList,
                                                   TCAPIHelper.DEFAULT_LOCALE,
                                                   aSW.getMillis (),
                                                   null,
                                                   null);
        aJson.addJson ("validation-results", aJsonVR);

        bValidationOK = aValidationResultList.containsNoError ();
      }

      if (bValidationOK)
      {
        MERoutingInformation aRoutingInfoFinal = null;
        final IJsonObject aJsonSMP = new JsonObject ();
        // Main query
        final ServiceMetadataType aSM = TCAPIHelper.querySMPServiceMetadata (aRoutingInfo.getReceiverID (),
                                                                             aRoutingInfo.getDocumentTypeID (),
                                                                             aRoutingInfo.getProcessID (),
                                                                             aRoutingInfo.getTransportProtocol ());
        if (aSM != null)
        {
          aJsonSMP.addJson ("response",
                            SMPJsonResponse.convert (aRoutingInfo.getReceiverID (),
                                                     aRoutingInfo.getDocumentTypeID (),
                                                     aSM));

          final EndpointType aEndpoint = IDDServiceMetadataProvider.getEndpoint (aSM,
                                                                                 aRoutingInfo.getProcessID (),
                                                                                 aRoutingInfo.getTransportProtocol ());
          if (aEndpoint != null)
          {
            aJsonSMP.add (SMPJsonResponse.JSON_ENDPOINT_REFERENCE, aEndpoint.getEndpointURI ());
            aRoutingInfoFinal = MERoutingInformation.create (aRoutingInfo,
                                                             aEndpoint.getEndpointURI (),
                                                             CertificateHelper.convertByteArrayToCertficateDirect (aEndpoint.getCertificate ()));
          }
          if (aRoutingInfoFinal == null)
          {
            LOGGER.warn ("[API] The SMP lookup for '" +
                         aRoutingInfo.getReceiverID ().getURIEncoded () +
                         "' and '" +
                         aRoutingInfo.getDocumentTypeID ().getURIEncoded () +
                         "' succeeded, but no endpoint matching '" +
                         aRoutingInfo.getProcessID ().getURIEncoded () +
                         "' and '" +
                         aRoutingInfo.getTransportProtocol () +
                         "' was found.");
          }

          // Only if a match was found
          aJsonSMP.add (JSON_SUCCESS, aRoutingInfoFinal != null);
        }
        else
          aJsonSMP.add (JSON_SUCCESS, false);
        aJson.addJson ("lookup-results", aJsonSMP);

        // Read for AS4 sending?
        if (aRoutingInfoFinal != null)
        {
          final IJsonObject aJsonSending = new JsonObject ();

          // Add payloads
          final MEMessage.Builder aMessage = MEMessage.builder ();
          for (final TCPayload aPayload : aOutgoingMsg.getPayload ())
          {
            aMessage.addPayload (MEPayload.builder ()
                                          .mimeType (MimeTypeParser.parseMimeType (aPayload.getMimeType ()))
                                          .contentID (StringHelper.getNotEmpty (aPayload.getContentID (),
                                                                                MEPayload.createRandomContentID ()))
                                          .data (aPayload.getValue ()));
          }
          TCAPIHelper.sendAS4Message (aRoutingInfoFinal, aMessage.build ());
          aJsonSending.add (JSON_SUCCESS, true);

          aJson.addJson ("sending-results", aJsonSending);
          bOverallSuccess = true;
        }

        // Overall success
        aJson.add (JSON_SUCCESS, bOverallSuccess);
      }
    });

    return aJson;
  }
}
