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
package com.helger.rdc.webapi.validation;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.timing.StopWatch;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.phive.api.executorset.VESID;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.phive.json.PhiveJsonHelper;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.rdc.core.api.RdcApiHelper;
import com.helger.rdc.core.validation.RdcValidator;
import com.helger.rdc.webapi.ApiParamException;
import com.helger.rdc.webapi.helper.AbstractRdcApiInvoker;
import com.helger.rdc.webapi.helper.CommonApiInvoker;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * Perform validation via API
 *
 * @author Philip Helger
 */
public class ApiPostValidateIem extends AbstractRdcApiInvoker
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ApiPostValidateIem.class);

  public ApiPostValidateIem ()
  {}

  @Override
  public IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                @Nonnull @Nonempty final String sPath,
                                @Nonnull final Map <String, String> aPathVariables,
                                @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException
  {
    final String sVESID = aPathVariables.get ("vesid");
    final VESID aVESID = VESID.parseIDOrNull (sVESID);
    if (aVESID == null)
      throw new ApiParamException ("Invalid VESID '" + sVESID + "' provided.");

    final byte [] aPayload = StreamHelper.getAllBytes (aRequestScope.getRequest ().getInputStream ());

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("API validating " + aPayload.length + " bytes using '" + aVESID.getAsSingleID () + "'");

    final IJsonObject aJson = new JsonObject ();
    CommonApiInvoker.invoke (aJson, () -> {
      // Main validation
      final StopWatch aSW = StopWatch.createdStarted ();
      final ValidationResultList aValidationResultList = RdcApiHelper.validateBusinessDocument (aVESID, aPayload);
      aSW.stop ();

      // Build response
      aJson.add (JSON_SUCCESS, true);
      PhiveJsonHelper.applyValidationResultList (aJson,
                                                 RdcValidator.getVES (aVESID),
                                                 aValidationResultList,
                                                 RdcApiHelper.DEFAULT_LOCALE,
                                                 aSW.getMillis (),
                                                 null,
                                                 null);
    });

    return aJson;
  }
}
