package com.helger.dcng.webapi.smp;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.core.api.DcngApiHelper;
import com.helger.dcng.webapi.ApiParamException;
import com.helger.dcng.webapi.helper.AbstractDcngApiInvoker;
import com.helger.dcng.webapi.helper.CommonApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Query all matching endpoints from an SMP
 *
 * @author Philip Helger
 */
public class ApiGetSmpEndpoints extends AbstractDcngApiInvoker
{
  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();

    // Get participant ID
    final String sParticipantID = aPathVariables.get ("pid");
    final IParticipantIdentifier aParticipantID = aIF.parseParticipantIdentifier (sParticipantID);
    if (aParticipantID == null)
      throw new ApiParamException ("Invalid participant ID '" + sParticipantID + "' provided.");

    // Get document type ID
    final String sDocTypeID = aPathVariables.get ("doctypeid");
    final IDocumentTypeIdentifier aDocTypeID = aIF.parseDocumentTypeIdentifier (sDocTypeID);
    if (aDocTypeID == null)
      throw new ApiParamException ("Invalid document type ID '" + sDocTypeID + "' provided.");

    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "[API] Participant information of '" +
                                aParticipantID.getURIEncoded () +
                                "' is queried for document type '" +
                                aDocTypeID.getURIEncoded () +
                                "'");

    // Start response
    final IJsonObject aJson = new JsonObject ();
    aJson.add (SMPJsonResponse.JSON_PARTICIPANT_ID, aParticipantID.getURIEncoded ());
    aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, aDocTypeID.getURIEncoded ());

    CommonApiInvoker.invoke (aJson, () -> {
      // Main query
      final ServiceMetadataType aSM = DcngApiHelper.querySMPServiceMetadata (aParticipantID,
                                                                            aDocTypeID,
                                                                            aIF.createProcessIdentifier ("dummy-procid", "procid-fake"),
                                                                            ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4.getID ());

      // Add to response
      if (aSM != null)
      {
        aJson.add (JSON_TAG_SUCCESS, true);
        aJson.addJson ("response", SMPJsonResponse.convert (aParticipantID, aDocTypeID, aSM));
      }
      else
        aJson.add (JSON_TAG_SUCCESS, false);
    });

    return aJson;
  }
}
