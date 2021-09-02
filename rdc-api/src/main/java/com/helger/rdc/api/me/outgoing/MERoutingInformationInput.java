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
package com.helger.rdc.api.me.outgoing;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.api.rest.TCOutgoingMetadata;

/**
 * Base class for {@link MERoutingInformation}
 *
 * @author Philip Helger
 */
public class MERoutingInformationInput
{
  private final IParticipantIdentifier m_aSenderID;
  private final IParticipantIdentifier m_aReceiverID;
  private final IDocumentTypeIdentifier m_aDocTypeID;
  private final IProcessIdentifier m_aProcessID;
  private final String m_sTransportProtocol;

  protected MERoutingInformationInput (@Nonnull final IParticipantIdentifier aSenderID,
                                       @Nonnull final IParticipantIdentifier aReceiverID,
                                       @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                       @Nonnull final IProcessIdentifier aProcessID,
                                       @Nonnull @Nonempty final String sTransportProtocol)
  {
    ValueEnforcer.notNull (aSenderID, "SenderID");
    ValueEnforcer.notNull (aReceiverID, "ReceiverID");
    ValueEnforcer.notNull (aDocTypeID, "DocTypeID");
    ValueEnforcer.notNull (aProcessID, "ProcessID");
    ValueEnforcer.notEmpty (sTransportProtocol, "TransportProtocol");

    m_aSenderID = aSenderID;
    m_aReceiverID = aReceiverID;
    m_aDocTypeID = aDocTypeID;
    m_aProcessID = aProcessID;
    m_sTransportProtocol = sTransportProtocol;
  }

  @Nonnull
  public final IParticipantIdentifier getSenderID ()
  {
    return m_aSenderID;
  }

  @Nonnull
  public final IParticipantIdentifier getReceiverID ()
  {
    return m_aReceiverID;
  }

  @Nonnull
  public final IDocumentTypeIdentifier getDocumentTypeID ()
  {
    return m_aDocTypeID;
  }

  @Nonnull
  public final IProcessIdentifier getProcessID ()
  {
    return m_aProcessID;
  }

  @Nonnull
  @Nonempty
  public final String getTransportProtocol ()
  {
    return m_sTransportProtocol;
  }

  @Override
  public String toString ()
  {
    return new ToStringGenerator (this).append ("SenderID", m_aSenderID)
                                       .append ("ReceiverID", m_aReceiverID)
                                       .append ("DocTypeID", m_aDocTypeID)
                                       .append ("ProcID", m_aProcessID)
                                       .append ("TransportProtocol", m_sTransportProtocol)
                                       .getToString ();
  }

  @Nonnull
  public static MERoutingInformationInput createBaseForSending (@Nonnull final TCOutgoingMetadata aMetadata)
  {
    ValueEnforcer.notNull (aMetadata, "Metadata");
    final IIdentifierFactory aIF = RdcConfig.getIdentifierFactory ();
    return new MERoutingInformationInput (aIF.createParticipantIdentifier (aMetadata.getSenderID ().getScheme (),
                                                                           aMetadata.getSenderID ().getValue ()),
                                          aIF.createParticipantIdentifier (aMetadata.getReceiverID ().getScheme (),
                                                                           aMetadata.getReceiverID ().getValue ()),
                                          aIF.createDocumentTypeIdentifier (aMetadata.getDocTypeID ().getScheme (),
                                                                            aMetadata.getDocTypeID ().getValue ()),
                                          aIF.createProcessIdentifier (aMetadata.getProcessID ().getScheme (),
                                                                       aMetadata.getProcessID ().getValue ()),
                                          aMetadata.getTransportProtocol ());
  }
}
