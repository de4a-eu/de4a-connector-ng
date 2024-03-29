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

import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.error.IError;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.error.list.ErrorList;

/**
 * Collecting implementation of {@link IDcngErrorHandler}
 *
 * @author Philip Helger
 */
public class WrappedDcngErrorHandler implements IDcngErrorHandler
{
  private final ErrorList m_aErrorList;
  private final Predicate <? super IError> m_aFilter;

  /**
   * Constructor collecting all errors.
   *
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   */
  public WrappedDcngErrorHandler (@Nonnull final ErrorList aErrorList)
  {
    this (aErrorList, null);
  }

  /**
   * Constructor collecting all errors matching the provided filter.
   *
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @param aFilter
   *        The filter to be used. May be <code>null</code> to collect all
   *        errors.
   */
  public WrappedDcngErrorHandler (@Nonnull final ErrorList aErrorList, @Nullable final Predicate <? super IError> aFilter)
  {
    m_aErrorList = aErrorList;
    m_aFilter = aFilter;
  }

  public void onMessage (@Nonnull final EErrorLevel eErrorLevel,
                         @Nonnull final String sMsg,
                         @Nullable final Throwable t,
                         @Nonnull final IDcngErrorCode eCode)
  {
    final IError aError = SingleError.builder ()
                                     .errorLevel (eErrorLevel)
                                     .errorText (sMsg)
                                     .errorID (eCode.getID ())
                                     .linkedException (t)
                                     .build ();
    if (m_aFilter == null || m_aFilter.test (aError))
      m_aErrorList.add (aError);
  }
}
