package com.helger.dcng.api.error;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.error.level.EErrorLevel;

/**
 * Custom DE4A Connector error handler callback
 *
 * @author Philip Helger
 */
public interface IDcngErrorHandler
{
  /**
   * The main error handler method to be implemented
   *
   * @param eErrorLevel
   *        Error level. Never <code>null</code>.
   * @param sMsg
   *        Error text. Never <code>null</code>.
   * @param t
   *        Optional exception. May be <code>null</code>.
   * @param eCode
   *        The DCNG specific error code. Never <code>null</code>.
   */
  void onMessage (@Nonnull EErrorLevel eErrorLevel, @Nonnull String sMsg, @Nullable Throwable t, @Nonnull IDcngErrorCode eCode);

  default void onWarning (@Nonnull final String sMsg, @Nonnull final IDcngErrorCode eCode)
  {
    onMessage (EErrorLevel.WARN, sMsg, null, eCode);
  }

  default void onWarning (@Nonnull final String sMsg, @Nullable final Throwable t, @Nonnull final IDcngErrorCode eCode)
  {
    onMessage (EErrorLevel.WARN, sMsg, t, eCode);
  }

  default void onError (@Nonnull final String sMsg, @Nonnull final IDcngErrorCode eCode)
  {
    onMessage (EErrorLevel.ERROR, sMsg, null, eCode);
  }

  default void onError (@Nonnull final String sMsg, @Nullable final Throwable t, @Nonnull final IDcngErrorCode eCode)
  {
    onMessage (EErrorLevel.ERROR, sMsg, t, eCode);
  }
}
