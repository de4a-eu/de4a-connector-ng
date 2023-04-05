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
