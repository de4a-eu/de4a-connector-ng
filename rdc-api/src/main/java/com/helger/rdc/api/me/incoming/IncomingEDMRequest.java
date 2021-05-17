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
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.hashcode.HashCodeGenerator;
import com.helger.commons.string.ToStringGenerator;

/**
 * Incoming EDM request. Uses {@link Object} and
 * {@link IMEIncomingTransportMetadata} for the metadata.
 *
 * @author Philip Helger
 */
@NotThreadSafe
public class IncomingEDMRequest implements IIncomingEDMRequest
{
  // TODO type
  private final IWritableObject m_aRequest;
  private final String m_sTopLevelContentID;
  private final IMEIncomingTransportMetadata m_aMetadata;

  public IncomingEDMRequest (@Nonnull final IWritableObject aRequest,
                             @Nonnull @Nonempty final String sTopLevelContentID,
                             @Nonnull final IMEIncomingTransportMetadata aMetadata)
  {
    ValueEnforcer.notNull (aRequest, "Request");
    ValueEnforcer.notEmpty (sTopLevelContentID, "TopLevelContentID");
    ValueEnforcer.notNull (aMetadata, "Metadata");
    m_aRequest = aRequest;
    m_sTopLevelContentID = sTopLevelContentID;
    m_aMetadata = aMetadata;
  }

  /**
   * @return The EDM request that contains the main payload. Never
   *         <code>null</code>.
   */
  @Nonnull
  public IWritableObject getRequest ()
  {
    return m_aRequest;
  }

  @Nonnull
  @Nonempty
  public String getTopLevelContentID ()
  {
    return m_sTopLevelContentID;
  }

  @Nonnull
  public IMEIncomingTransportMetadata getMetadata ()
  {
    return m_aMetadata;
  }

  @Override
  public boolean equals (final Object o)
  {
    if (o == this)
      return true;
    if (o == null || !getClass ().equals (o.getClass ()))
      return false;

    final IncomingEDMRequest rhs = (IncomingEDMRequest) o;
    return m_aRequest.equals (rhs.m_aRequest) && m_aMetadata.equals (rhs.m_aMetadata);
  }

  @Override
  public int hashCode ()
  {
    return new HashCodeGenerator (this).append (m_aRequest).append (m_aMetadata).getHashCode ();
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("Request", m_aRequest)
                                       .append ("TopLevelContentID", m_sTopLevelContentID)
                                       .append ("Metadata", m_aMetadata)
                                       .getToString ();
  }
}
