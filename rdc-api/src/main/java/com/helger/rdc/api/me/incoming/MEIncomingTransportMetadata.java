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
import javax.annotation.concurrent.Immutable;

import com.helger.commons.equals.EqualsHelper;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;

/**
 * Container for all relevant AS4 transport metadata that may be interesting to
 * the recipient.
 *
 * @author Philip Helger
 */
@Immutable
public class MEIncomingTransportMetadata implements IMEIncomingTransportMetadata
{
  private final IParticipantIdentifier m_aSenderID;
  private final IParticipantIdentifier m_aReceiverID;
  private final IDocumentTypeIdentifier m_aDocTypeID;
  private final IProcessIdentifier m_aProcessID;

  public MEIncomingTransportMetadata (@Nullable final IParticipantIdentifier aSenderID,
                                      @Nullable final IParticipantIdentifier aReceiverID,
                                      @Nullable final IDocumentTypeIdentifier aDocTypeID,
                                      @Nullable final IProcessIdentifier aProcessID)
  {
    m_aSenderID = aSenderID;
    m_aReceiverID = aReceiverID;
    m_aDocTypeID = aDocTypeID;
    m_aProcessID = aProcessID;
  }

  @Nullable
  public IParticipantIdentifier getSenderID ()
  {
    return m_aSenderID;
  }

  @Nullable
  public IParticipantIdentifier getReceiverID ()
  {
    return m_aReceiverID;
  }

  @Nullable
  public IDocumentTypeIdentifier getDocumentTypeID ()
  {
    return m_aDocTypeID;
  }

  @Nullable
  public IProcessIdentifier getProcessID ()
  {
    return m_aProcessID;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;

    final MEIncomingTransportMetadata rhs = (MEIncomingTransportMetadata) o;
    return EqualsHelper.equalsCustom (m_aSenderID, rhs.m_aSenderID, IParticipantIdentifier::hasSameContent) &&
           EqualsHelper.equalsCustom (m_aReceiverID, rhs.m_aReceiverID, IParticipantIdentifier::hasSameContent) &&
           EqualsHelper.equalsCustom (m_aDocTypeID, rhs.m_aDocTypeID, IDocumentTypeIdentifier::hasSameContent) &&
           EqualsHelper.equalsCustom (m_aProcessID, rhs.m_aProcessID, IProcessIdentifier::hasSameContent);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aSenderID)
                                       .append (m_aReceiverID)
                                       .append (m_aDocTypeID)
                                       .append (m_aProcessID)
                                       .getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SenderID", m_aSenderID)
                                       .append ("ReceiverID", m_aReceiverID)
                                       .append ("DocumentTypeID", m_aDocTypeID)
                                       .append ("ProcessID", m_aProcessID)
                                       .getToString ();
  }
}
