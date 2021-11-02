/*
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
package com.helger.dcng.api.me.incoming;

import javax.annotation.Nonnull;

import com.helger.dcng.api.me.model.MEMessage;

/**
 * The callback handler for incoming messages from the AS4 Gateway. An
 * implementation of this interface must be provided when calling
 * "RdcInit.initGlobally". The default implementation is
 * "RdcIncomingHandlerViaHttp". If you are embedding the RDC into your
 * application you must provide an implementation of this interface.
 *
 * @author Philip Helger
 */
public interface IMEIncomingHandler
{
  /**
   * Handle an incoming request (for DC or DP).
   *
   * @param aMessage
   *        The message to handle. Never <code>null</code>.
   * @throws MEIncomingException
   *         In case of error.
   */
  void handleIncomingRequest (@Nonnull MEMessage aMessage) throws MEIncomingException;
}
