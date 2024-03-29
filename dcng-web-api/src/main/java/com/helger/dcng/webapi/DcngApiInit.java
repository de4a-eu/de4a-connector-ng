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
package com.helger.dcng.webapi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.dcng.webapi.as4.ApiPostLookupAndSendIt2;
import com.helger.dcng.webapi.as4.ApiPostSendIt2;
import com.helger.dcng.webapi.smp.ApiGetSmpDocTypes;
import com.helger.dcng.webapi.smp.ApiGetSmpEndpoints;
import com.helger.photon.api.APIDescriptor;
import com.helger.photon.api.APIPath;
import com.helger.photon.api.IAPIRegistry;

/**
 * Register all APIs
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngApiInit
{
  private DcngApiInit ()
  {}

  public static void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    // SMP stuff
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.get ("/smp/doctypes/{pid}"), ApiGetSmpDocTypes.class));
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.get ("/smp/endpoints/{pid}/{doctypeid}"),
                                                 ApiGetSmpEndpoints.class));

    // AS4 stuff
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.post ("/it2/send"), ApiPostSendIt2.class));
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.post ("/it2/lookup/send"), new ApiPostLookupAndSendIt2 ()));
  }
}
