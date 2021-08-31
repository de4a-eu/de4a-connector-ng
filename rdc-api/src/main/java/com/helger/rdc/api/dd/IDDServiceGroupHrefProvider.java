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
package com.helger.rdc.api.dd;

import javax.annotation.Nonnull;

import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.rdc.api.error.IRdcErrorHandler;

/**
 * Helper interface to be used by the REST API.
 *
 * @author Philip Helger
 */
public interface IDDServiceGroupHrefProvider
{
  /**
   * @param aParticipantID
   *        Participant ID to query.
   * @param aErrorHandler
   *        The error handler to be used. May not be <code>null</code>.
   * @return A non-<code>null</code> sorted map of all hrefs. The key MUST be
   *         URL decoded whereas the value is the "original href" as found in
   *         the response.
   */
  @Nonnull
  ICommonsSortedMap <String, String> getAllServiceGroupHrefs (@Nonnull IParticipantIdentifier aParticipantID,
                                                              @Nonnull IRdcErrorHandler aErrorHandler);
}
