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
package com.helger.dcng.webapi.helper;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;

import com.helger.commons.CGlobal;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.OverrideOnDemand;
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
 * Abstract base invoker for DCNG REST API
 *
 * @author Philip Helger
 */
public abstract class AbstractDcngApiInvoker implements IAPIExecutor
{
  public static final String JSON_TAG_SUCCESS = "success";

  /**
   * @param aRequestScope
   *        Current request
   * @return <code>true</code> to cache the result, <code>false</code> to not
   *         cache it.
   */
  @OverrideOnDemand
  protected boolean isCacheResult (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    return aRequestScope.getHttpMethod () == EHttpMethod.GET;
  }

  /**
   * @param aRequestScope
   *        Current request
   * @return Number of seconds to cache. Must be &gt; 0.
   * @see #isCacheResult(IRequestWebScopeWithoutResponse)
   */
  @OverrideOnDemand
  protected int getCachingSeconds (@Nonnull final IRequestWebScopeWithoutResponse aRequestScope)
  {
    return 3 * CGlobal.SECONDS_PER_HOUR;
  }

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

    final boolean bSuccess = aJson.getAsBoolean (JSON_TAG_SUCCESS, false);
    if (!bSuccess)
      aPUR.setAllowContentOnStatusCode (true).setStatus (CHttp.HTTP_BAD_REQUEST);
    else
      if (isCacheResult (aRequestScope))
        aPUR.enableCaching (getCachingSeconds (aRequestScope));
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
