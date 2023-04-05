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
package com.helger.dcng.api.me.outgoing;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.string.ToStringGenerator;
import com.helger.dcng.api.rest.DCNGOutgoingMetadata;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.security.certificate.CertificateHelper;

/**
 * Default implementation of {@link IMERoutingInformation}.
 *
 * @author Philip Helger
 */
public class MERoutingInformation extends MERoutingInformationInput implements IMERoutingInformation
{
  private final String m_sEndpointURL;
  private final X509Certificate m_aCert;

  public MERoutingInformation (@Nonnull final IParticipantIdentifier aSenderID,
                               @Nonnull final IParticipantIdentifier aReceiverID,
                               @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                               @Nonnull final IProcessIdentifier aProcessID,
                               @Nonnull @Nonempty final String sTransportProtocol,
                               @Nonnull @Nonempty final String sEndpointURL,
                               @Nonnull final X509Certificate aCert)
  {
    super (aSenderID, aReceiverID, aDocTypeID, aProcessID, sTransportProtocol);
    ValueEnforcer.notEmpty (sEndpointURL, "EndpointURL");
    ValueEnforcer.notNull (aCert, "Cert");

    m_sEndpointURL = sEndpointURL;
    m_aCert = aCert;
  }

  @Nonnull
  @Nonempty
  public final String getEndpointURL ()
  {
    return m_sEndpointURL;
  }

  @Nonnull
  public final X509Certificate getCertificate ()
  {
    return m_aCert;
  }

  @Override
  public String toString ()
  {
    return ToStringGenerator.getDerived (super.toString ())
                            .append ("EndpointURL", m_sEndpointURL)
                            .append ("Certificate", m_aCert)
                            .getToString ();
  }

  @Nonnull
  public static MERoutingInformation create (@Nonnull final MERoutingInformationInput aOther,
                                             @Nonnull @Nonempty final String sEndpointURL,
                                             @Nonnull final X509Certificate aCert)
  {
    ValueEnforcer.notNull (aOther, "Other");
    return new MERoutingInformation (aOther.getSenderID (),
                                     aOther.getReceiverID (),
                                     aOther.getDocumentTypeID (),
                                     aOther.getProcessID (),
                                     aOther.getTransportProtocol (),
                                     sEndpointURL,
                                     aCert);
  }

  @Nonnull
  public static MERoutingInformation createForSending (@Nonnull final DCNGOutgoingMetadata aMetadata) throws CertificateException
  {
    ValueEnforcer.notNull (aMetadata, "Metadata");
    return create (createBaseForSending (aMetadata),
                   aMetadata.getEndpointURL (),
                   CertificateHelper.convertByteArrayToCertficateDirect (aMetadata.getReceiverCertificate ()));
  }
}
