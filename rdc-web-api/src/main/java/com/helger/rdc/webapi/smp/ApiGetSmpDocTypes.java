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
package com.helger.rdc.webapi.smp;

import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppol.sml.ESMPAPIType;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.core.api.RdcApiHelper;
import com.helger.rdc.webapi.ApiParamException;
import com.helger.rdc.webapi.helper.AbstractRdcApiInvoker;
import com.helger.rdc.webapi.helper.CommonApiInvoker;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Query all document types of a participant
 *
 * @author Philip Helger
 */
public class ApiGetSmpDocTypes extends AbstractRdcApiInvoker
{
  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    final String sParticipantID = aPathVariables.get ("pid");
    final IParticipantIdentifier aParticipantID = RdcConfig.getIdentifierFactory ().parseParticipantIdentifier (sParticipantID);
    if (aParticipantID == null)
      throw new ApiParamException ("Invalid participant ID '" + sParticipantID + "' provided.");

    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "[API] Document types of '" + aParticipantID.getURIEncoded () + "' are queried");

    final IJsonObject aJson = new JsonObject ();
    aJson.add (SMPJsonResponse.JSON_PARTICIPANT_ID, aParticipantID.getURIEncoded ());
    CommonApiInvoker.invoke (aJson, () -> {
      // Query SMP
      final ICommonsSortedMap <String, String> aSGHrefs = RdcApiHelper.querySMPServiceGroups (aParticipantID);

      aJson.add (JSON_SUCCESS, true);
      aJson.addJson ("response",
                     SMPJsonResponse.convert (ESMPAPIType.OASIS_BDXR_V1, aParticipantID, aSGHrefs, RdcConfig.getIdentifierFactory ()));
    });

    return aJson;
  }
}
