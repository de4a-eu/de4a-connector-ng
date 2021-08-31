/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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
package com.helger.rdc.api.validation;

import java.util.Locale;

import javax.annotation.Nonnull;

import com.helger.phive.api.executorset.VESID;
import com.helger.phive.api.result.ValidationResultList;

/**
 * RDC Validation Service
 *
 * @author Philip Helger
 */
public interface IRdcValidator
{
  /**
   * Perform validation
   *
   * @param aVESID
   *        VESID to use.
   * @param aPayload
   *        Payload to validate.
   * @param aDisplayLocale
   *        Display locale for the error message.
   * @return A non-<code>null</code> result list.
   */
  @Nonnull
  ValidationResultList validate (@Nonnull VESID aVESID, @Nonnull byte [] aPayload, @Nonnull Locale aDisplayLocale);
}
