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
package com.helger.rdc.api.me.incoming;

import javax.annotation.Nonnull;

/**
 * The callback handler for incoming messages from the AS4 Gateway. An
 * implementation of this interface must be provided when calling
 * "TCInit.initGlobally". The default implementation is
 * "TCIncomingHandlerViaHttp". If you are embedding the TC into your application
 * you must provide an implementation of this interface.
 *
 * @author Philip Helger
 */
public interface IMEIncomingHandler
{
  /**
   * Handle an incoming request for step 2/4 (on DP side).
   *
   * @param aRequest
   *        The request to handle. Never <code>null</code>.
   * @throws MEIncomingException
   *         In case of error.
   */
  void handleIncomingRequest (@Nonnull IncomingEDMRequest aRequest) throws MEIncomingException;

  /**
   * Handle an incoming response for step 4/4 (on DC side).
   *
   * @param aResponse
   *        The response to handle. Contains attachments and metadata. Never
   *        <code>null</code>.
   * @throws MEIncomingException
   *         In case of error.
   */
  void handleIncomingResponse (@Nonnull IncomingEDMResponse aResponse) throws MEIncomingException;
}
