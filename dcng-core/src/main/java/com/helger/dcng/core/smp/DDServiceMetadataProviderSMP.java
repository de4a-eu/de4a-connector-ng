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
package com.helger.dcng.core.smp;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.dcng.api.dd.IDDServiceMetadataProvider;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.smpclient.bdxr1.IBDXRServiceMetadataProvider;
import com.helger.smpclient.exception.SMPClientException;
import com.helger.smpclient.url.SMPDNSResolutionException;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;
import com.helger.xsds.bdxr.smp1.SignedServiceMetadataType;

/**
 * An implementation of {@link IDDServiceMetadataProvider} going to the SMP for
 * querying.
 *
 * @author Philip Helger
 */
public class DDServiceMetadataProviderSMP extends AbstractDDClient implements IDDServiceMetadataProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DDServiceMetadataProviderSMP.class);

  public DDServiceMetadataProviderSMP ()
  {}

  @Nullable
  public ServiceMetadataType getServiceMetadata (@Nonnull final IParticipantIdentifier aParticipantID,
                                                 @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                                 @Nonnull final IProcessIdentifier aProcessID,
                                                 @Nonnull final String sTransportProfile)
  {
    ValueEnforcer.notNull (aParticipantID, "ParticipantID");
    ValueEnforcer.notNull (aDocTypeID, "DocTypeID");

    try
    {
      final IBDXRServiceMetadataProvider aBDXR1Client = getServiceMetadataProvider (aParticipantID,
                                                                                    aDocTypeID,
                                                                                    aProcessID,
                                                                                    sTransportProfile);

      final SignedServiceMetadataType aSSM = aBDXR1Client.getServiceMetadataOrNull (aParticipantID, aDocTypeID);
      if (aSSM == null)
        return null;
      return aSSM.getServiceMetadata ();
    }
    catch (final SMPDNSResolutionException | SMPClientException ex)
    {
      LOGGER.error ("getServiceMetadata exception: " + ex.getClass ().getName () + " - " + ex.getMessage ());
      throw new IllegalStateException (ex);
    }
  }
}
