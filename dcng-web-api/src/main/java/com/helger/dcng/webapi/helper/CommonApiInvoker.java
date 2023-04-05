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

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.callback.IThrowingRunnable;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.timing.StopWatch;
import com.helger.dcng.webapi.as4.LookupAndSendingResult;
import com.helger.json.IJsonObject;
import com.helger.phive.json.PhiveJsonHelper;

@Immutable
public final class CommonApiInvoker
{
  private CommonApiInvoker ()
  {}

  public static void invoke (@Nonnull final LookupAndSendingResult aResult, @Nonnull final IThrowingRunnable <Exception> r)
  {
    final ZonedDateTime aInvocationDT = PDTFactory.getCurrentZonedDateTimeUTC ();
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      r.run ();
    }
    catch (final Exception ex)
    {
      aResult.setOverallSuccess (false);
      aResult.setException (ex);
    }
    aSW.stop ();

    aResult.setInvocationDT (aInvocationDT);
    aResult.setDurationMS (aSW.getMillis ());
  }

  public static void invoke (@Nonnull final IJsonObject aJson, @Nonnull final IThrowingRunnable <Exception> r)
  {
    final ZonedDateTime aInvocationDT = PDTFactory.getCurrentZonedDateTimeUTC ();
    final StopWatch aSW = StopWatch.createdStarted ();
    try
    {
      r.run ();
    }
    catch (final Exception ex)
    {
      aJson.add (AbstractDcngApiInvoker.JSON_TAG_SUCCESS, false);
      aJson.addJson (LookupAndSendingResult.JSON_TAG_EXCEPTION, PhiveJsonHelper.getJsonStackTrace (ex));
    }
    aSW.stop ();

    aJson.add (LookupAndSendingResult.JSON_TAG_INVOCATION_DATE_TIME, DateTimeFormatter.ISO_ZONED_DATE_TIME.format (aInvocationDT));
    aJson.add (LookupAndSendingResult.JSON_TAG_INVOCATION_DURATION_MILLIS, aSW.getMillis ());
  }
}
