package com.helger.dcng.api.error;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.log.LogHelper;

/**
 * Logging implementation of {@link IDcngErrorHandler}
 *
 * @author Philip Helger
 */
public class LoggingDcngErrorHandler implements IDcngErrorHandler
{
  public static final LoggingDcngErrorHandler INSTANCE = new LoggingDcngErrorHandler ();
  private static final Logger LOGGER = LoggerFactory.getLogger (LoggingDcngErrorHandler.class);

  public void onMessage (@Nonnull final EErrorLevel eErrorLevel,
                         @Nonnull final String sMsg,
                         @Nullable final Throwable t,
                         @Nonnull final IDcngErrorCode eCode)
  {
    LogHelper.log (LOGGER, eErrorLevel, sMsg, t);
  }
}
