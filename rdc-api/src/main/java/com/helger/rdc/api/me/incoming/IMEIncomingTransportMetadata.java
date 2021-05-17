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

import javax.annotation.Nullable;

import com.helger.commons.annotation.MustImplementEqualsAndHashcode;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;

/**
 * Read-only interface for incoming transport metadata.
 *
 * @author Philip Helger
 */
@MustImplementEqualsAndHashcode
public interface IMEIncomingTransportMetadata
{
  /**
   * @return Sender participant ID. May be <code>null</code>.
   */
  @Nullable
  IParticipantIdentifier getSenderID ();

  /**
   * @return Receiver participant ID. May be <code>null</code>.
   */
  @Nullable
  IParticipantIdentifier getReceiverID ();

  /**
   * @return Document type ID. May be <code>null</code>.
   */
  @Nullable
  IDocumentTypeIdentifier getDocumentTypeID ();

  /**
   * @return Process ID. May be <code>null</code>.
   */
  @Nullable
  IProcessIdentifier getProcessID ();
}
