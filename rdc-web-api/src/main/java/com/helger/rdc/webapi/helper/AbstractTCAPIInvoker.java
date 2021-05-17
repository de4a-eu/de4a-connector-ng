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
package com.helger.rdc.webapi.helper;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.commons.timing.StopWatch;
import com.helger.json.IJsonObject;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.photon.api.IAPIDescriptor;
import com.helger.photon.api.IAPIExecutor;
import com.helger.photon.app.PhotonUnifiedResponse;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * Abstract base invoker for TC REST API
 *
 * @author Philip Helger
 */
public abstract class AbstractTCAPIInvoker implements IAPIExecutor
{
  protected static final String JSON_SUCCESS = "success";

  @Nonnull
  public abstract IJsonObject invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                                         @Nonnull @Nonempty final String sPath,
                                         @Nonnull final Map <String, String> aPathVariables,
                                         @Nonnull final IRequestWebScopeWithoutResponse aRequestScope) throws IOException;

  public final void invokeAPI (@Nonnull final IAPIDescriptor aAPIDescriptor,
                               @Nonnull @Nonempty final String sPath,
                               @Nonnull final Map <String, String> aPathVariables,
                               @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                               @Nonnull final UnifiedResponse aUnifiedResponse) throws Exception
  {
    final StopWatch aSW = StopWatch.createdStarted ();

    final IJsonObject aJson = invokeAPI (aAPIDescriptor, sPath, aPathVariables, aRequestScope);

    final PhotonUnifiedResponse aPUR = (PhotonUnifiedResponse) aUnifiedResponse;
    aPUR.setJsonWriterSettings (new JsonWriterSettings ().setIndentEnabled (true));
    aPUR.json (aJson);

    final boolean bSuccess = aJson.getAsBoolean (JSON_SUCCESS, false);
    if (!bSuccess)
      aPUR.setAllowContentOnStatusCode (true).setStatus (CHttp.HTTP_BAD_REQUEST);
    else
      if (aRequestScope.getHttpMethod () == EHttpMethod.GET)
        aPUR.enableCaching (3 * CGlobal.SECONDS_PER_HOUR);
      else
        aPUR.disableCaching ();

    aSW.stop ();

    DE4AKafkaClient.send (bSuccess ? EErrorLevel.INFO : EErrorLevel.ERROR,
                          () -> "[API] Finished '" +
                                aAPIDescriptor.getPathDescriptor ().getAsURLString () +
                                "' after " +
                                aSW.getMillis () +
                                " milliseconds with " +
                                (bSuccess ? "success" : "error"));
  }
}
