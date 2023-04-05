package com.helger.dcng.webapi.as4;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.dd.IDDServiceMetadataProvider;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.model.MEPayload;
import com.helger.dcng.api.me.outgoing.MERoutingInformation;
import com.helger.dcng.api.me.outgoing.MERoutingInformationInput;
import com.helger.dcng.api.rest.DCNGOutgoingMessage;
import com.helger.dcng.api.rest.DCNGPayload;
import com.helger.dcng.api.rest.DcngRestJAXB;
import com.helger.dcng.core.api.DcngApiHelper;
import com.helger.dcng.core.regrep.DcngRegRepHelperIt2;
import com.helger.dcng.webapi.ApiParamException;
import com.helger.dcng.webapi.helper.AbstractDcngApiInvoker;
import com.helger.dcng.webapi.helper.CommonApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.regrep.CRegRep4;
import com.helger.security.certificate.CertificateHelper;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Perform validation, lookup and sending via API
 *
 * @author Philip Helger
 */
public class ApiPostLookupAndSendIt2 extends AbstractDcngApiInvoker
{
  public ApiPostLookupAndSendIt2 ()
  {}

  @Nonnull
  public static LookupAndSendingResult perform (@Nonnull final IParticipantIdentifier aSenderID,
                                                @Nonnull final IParticipantIdentifier aReceiverID,
                                                @Nonnull final IDocumentTypeIdentifier aDocumentTypeID,
                                                @Nonnull final IProcessIdentifier aProcessID,
                                                @Nonnull final String sTransportProfile,
                                                @Nonnull final DCNGPayload aPayload)
  {
    // Start response
    final LookupAndSendingResult ret = new LookupAndSendingResult (aSenderID, aReceiverID, aDocumentTypeID, aProcessID, sTransportProfile);

    CommonApiInvoker.invoke (ret, () -> {
      boolean bOverallSuccess = false;
      MERoutingInformation aRoutingInfo = null;

      // Query SMP
      {
        // Remote SMP query
        final ServiceMetadataType aSM = DcngApiHelper.querySMPServiceMetadata (aReceiverID, aDocumentTypeID, aProcessID, sTransportProfile);
        if (aSM != null)
        {
          ret.setLookupServiceMetadata (aSM);

          // Search SMP results for matches
          final EndpointType aEndpoint = IDDServiceMetadataProvider.getEndpoint (aSM, aProcessID, sTransportProfile);
          if (aEndpoint != null)
          {
            ret.setLookupEndpointURL (aEndpoint.getEndpointURI ());
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
          ret.setLookupSuccess (aRoutingInfo != null);
        }
        else
          ret.setLookupSuccess (false);
      }

      // Read for AS4 sending?
      if (aRoutingInfo != null)
      {
        // Wrap in RegRep
        final byte [] aRegRepPayload = DcngRegRepHelperIt2.wrapInRegRep (true, aPayload.getValue ());

        // Add payload
        final MEMessage.Builder aMessage = MEMessage.builder ();
        aMessage.addPayload (MEPayload.builder ()
                                      .mimeType (CRegRep4.MIME_TYPE_EBRS_XML)
                                      .contentID (MEPayload.createRandomContentID ())
                                      .data (aRegRepPayload));

        // Trigger main sending with the chosen AS4 implementation
        DcngApiHelper.sendAS4Message (aRoutingInfo, aMessage.build ());
        ret.setSendingSuccess (true);

        // Remember sending stuff
        bOverallSuccess = true;
      }

      // Overall success
      ret.setOverallSuccess (bOverallSuccess);
    });

    return ret;

  }

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
                    aOutgoingMsg.getPayloadAtIndex (0)).getAsJson ();
  }
}
