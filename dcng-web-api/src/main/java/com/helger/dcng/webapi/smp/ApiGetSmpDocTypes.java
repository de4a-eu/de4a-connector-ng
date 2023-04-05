package com.helger.dcng.webapi.smp;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.core.api.DcngApiHelper;
import com.helger.dcng.webapi.ApiParamException;
import com.helger.dcng.webapi.helper.AbstractDcngApiInvoker;
import com.helger.dcng.webapi.helper.CommonApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Query all document types of a participant
 *
 * @author Philip Helger
 */
public class ApiGetSmpDocTypes extends AbstractDcngApiInvoker
{
  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    final String sParticipantID = aPathVariables.get ("pid");
    final IParticipantIdentifier aParticipantID = DcngConfig.getIdentifierFactory ().parseParticipantIdentifier (sParticipantID);
    if (aParticipantID == null)
      throw new ApiParamException ("Invalid participant ID '" + sParticipantID + "' provided.");

    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "[API] Document types of '" + aParticipantID.getURIEncoded () + "' are queried");

    final IJsonObject aJson = new JsonObject ();
    aJson.add (SMPJsonResponse.JSON_PARTICIPANT_ID, aParticipantID.getURIEncoded ());
    CommonApiInvoker.invoke (aJson, () -> {
      // Query SMP
      final ICommonsSortedMap <String, String> aSGHrefs = DcngApiHelper.querySMPServiceGroups (aParticipantID);

      aJson.add (JSON_TAG_SUCCESS, true);
      aJson.addJson ("response",
                     SMPJsonResponse.convert (ESMPAPIType.OASIS_BDXR_V1, aParticipantID, aSGHrefs, DcngConfig.getIdentifierFactory ()));
    });

    return aJson;
  }
}
