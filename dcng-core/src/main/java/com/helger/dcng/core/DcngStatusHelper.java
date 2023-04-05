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
package com.helger.dcng.core;

import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsLinkedHashMap;
import com.helger.commons.collection.impl.ICommonsOrderedMap;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.datetime.PDTWebDateHelper;
import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.system.SystemProperties;
import com.helger.config.source.res.IConfigurationSourceResource;
import com.helger.dcng.api.DcngConfig;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;

/**
 * Helper to create the Connector status reachable via the "/status/" servlet.
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngStatusHelper
{
  private DcngStatusHelper ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static IJsonObject getDefaultStatusData ()
  {
    final IJsonObject aStatusData = new JsonObject ();
    aStatusData.add ("build.version", CDcngVersion.BUILD_VERSION);
    aStatusData.add ("build.datetime", CDcngVersion.BUILD_TIMESTAMP);
    aStatusData.add ("status.datetime", PDTWebDateHelper.getAsStringXSD (PDTFactory.getCurrentZonedDateTimeUTC ()));
    aStatusData.add ("java.version", SystemProperties.getJavaVersion ());
    aStatusData.add ("global.debug", GlobalDebug.isDebugMode ());
    aStatusData.add ("global.production", GlobalDebug.isProductionMode ());

    // add all configuration items to status (all except passwords)
    final ICommonsOrderedMap <String, String> aVals = new CommonsLinkedHashMap <> ();
    DcngConfig.getConfig ().forEachConfigurationValueProvider ( (aCVP, nPriority) -> {
      if (aCVP instanceof IConfigurationSourceResource)
      {
        final ICommonsOrderedMap <String, String> aAll = ((IConfigurationSourceResource) aCVP).getAllConfigItems ();
        for (final Map.Entry <String, String> aEntry : aAll.entrySet ())
        {
          // Never override, because highest priority values come first
          if (!aVals.containsKey (aEntry.getKey ()))
            aVals.put (aEntry);
        }
      }
    });

    // Maintain the retrieved order
    for (final Map.Entry <String, String> aEntry : aVals.entrySet ())
    {
      final String sKey = aEntry.getKey ();
      if (sKey.contains ("password"))
        aStatusData.add (aEntry.getKey (), "***");
      else
        aStatusData.add (aEntry.getKey (), aEntry.getValue ());
    }

    return aStatusData;
  }
}
