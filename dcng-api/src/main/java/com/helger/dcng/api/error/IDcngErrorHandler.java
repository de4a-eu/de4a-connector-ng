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
