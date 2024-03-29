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
package com.helger.dcng.holodeck;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;

/**
 * Utilities related to date-time formatting, parsing etc..
 *
 * @author myildiz on 16.02.2018.
 */
public final class DateTimeUtils
{
  private DateTimeUtils ()
  {}

  /**
   * Create the current date time string in format uuuu-MM-dd'T'HH:mm:ss.SSSX
   *
   * @return current date time with milliseconds and timezone in UTC
   */
  @Nonnull
  @Nonempty
  public static String getCurrentTimestamp ()
  {
    final ZonedDateTime now = ZonedDateTime.now (ZoneOffset.UTC);
    return now.format (DateTimeFormatter.ofPattern ("uuuu-MM-dd'T'HH:mm:ss.SSSX", Locale.US));
  }
}
