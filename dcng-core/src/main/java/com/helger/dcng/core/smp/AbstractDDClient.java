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

import java.net.URI;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.url.URLHelper;
import com.helger.dcng.api.RdcConfig;
import com.helger.dcng.core.http.RdcHttpClientSettings;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.factory.BDXR1IdentifierFactory;
import com.helger.smpclient.bdxr1.BDXRClientReadOnly;
import com.helger.smpclient.bdxr1.IBDXRServiceGroupProvider;
import com.helger.smpclient.bdxr1.IBDXRServiceMetadataProvider;
import com.helger.smpclient.url.BDXLURLProvider;
import com.helger.smpclient.url.SMPDNSResolutionException;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ProcessListType;
import com.helger.xsds.bdxr.smp1.ProcessType;
import com.helger.xsds.bdxr.smp1.ServiceEndpointList;
import com.helger.xsds.bdxr.smp1.ServiceInformationType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;
import com.helger.xsds.bdxr.smp1.SignedServiceMetadataType;

/**
 * Base class for the dynamic discovery solutions. This class honors the
 * configuration file.
 *
 * @author Philip Helger
 */
public abstract class AbstractDDClient
{
  protected AbstractDDClient ()
  {}

  @Nonnull
  private static BDXRClientReadOnly _getSMPClient (@Nonnull final IParticipantIdentifier aRecipientID) throws SMPDNSResolutionException
  {
    final BDXRClientReadOnly ret;
    if (RdcConfig.SMP.isUseDNS ())
    {
      ValueEnforcer.notNull (aRecipientID, "RecipientID");

      // Use dynamic lookup via DNS - can throw exception
      ret = new BDXRClientReadOnly (BDXLURLProvider.INSTANCE, aRecipientID, RdcConfig.SMP.getSML ());
    }
    else
    {
      // Use a constant SMP URL
      final URI aSMPURI = RdcConfig.SMP.getStaticSMPUrl ();
      ret = new BDXRClientReadOnly (aSMPURI);
    }
    if (RdcConfig.SMP.isUseGlobalHttpSettings ())
      ret.httpClientSettings ().setAllFrom (new RdcHttpClientSettings ());
    return ret;
  }

  @Nonnull
  public static IBDXRServiceGroupProvider getServiceGroupProvider (@Nonnull final IParticipantIdentifier aRecipientID) throws SMPDNSResolutionException
  {
    return _getSMPClient (aRecipientID);
  }

  @Nonnull
  public static IBDXRServiceMetadataProvider getServiceMetadataProvider (@Nonnull final IParticipantIdentifier aRecipientID,
                                                                         @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                                                         @Nonnull final IProcessIdentifier aProcessID,
                                                                         @Nonnull final String sTransportProfile) throws SMPDNSResolutionException
  {
    if (!RdcConfig.SMP.isUseDNS ())
    {
      final String sStaticEndpoint = RdcConfig.SMP.getStaticEndpointURL ();
      final X509Certificate aStaticCert = RdcConfig.SMP.getStaticCertificate ();
      if (URLHelper.getAsURL (sStaticEndpoint) != null && aStaticCert != null)
      {
        // Create a static SignedServiceMetadataType object to return
        return (aServiceGroupID, aDocumentTypeID) -> {
          final SignedServiceMetadataType ret = new SignedServiceMetadataType ();
          final ServiceMetadataType aSM = new ServiceMetadataType ();
          final ServiceInformationType aSI = new ServiceInformationType ();
          aSI.setParticipantIdentifier (BDXR1IdentifierFactory.INSTANCE.createParticipantIdentifier (aServiceGroupID.getScheme (),
                                                                                                     aServiceGroupID.getValue ()));
          aSI.setDocumentIdentifier (BDXR1IdentifierFactory.INSTANCE.createDocumentTypeIdentifier (aDocTypeID.getScheme (),
                                                                                                   aDocTypeID.getValue ()));
          {
            final ProcessListType aPL = new ProcessListType ();
            final ProcessType aProcess = new ProcessType ();
            aProcess.setProcessIdentifier (BDXR1IdentifierFactory.INSTANCE.createProcessIdentifier (aProcessID.getScheme (),
                                                                                                    aProcessID.getValue ()));
            final ServiceEndpointList aSEL = new ServiceEndpointList ();
            final EndpointType aEndpoint = new EndpointType ();
            aEndpoint.setEndpointURI (sStaticEndpoint);
            aEndpoint.setRequireBusinessLevelSignature (Boolean.FALSE);
            try
            {
              aEndpoint.setCertificate (aStaticCert.getEncoded ());
            }
            catch (final CertificateEncodingException ex)
            {
              throw new IllegalArgumentException ("Failed to encode certificate " + aStaticCert);
            }
            aEndpoint.setServiceDescription ("Mocked service");
            aEndpoint.setTechnicalContactUrl ("Mocked service - no support");
            aEndpoint.setTransportProfile (sTransportProfile);
            aSEL.addEndpoint (aEndpoint);
            aProcess.setServiceEndpointList (aSEL);
            aPL.addProcess (aProcess);
            aSI.setProcessList (aPL);
          }
          aSM.setServiceInformation (aSI);
          ret.setServiceMetadata (aSM);
          return ret;
        };
      }
      // fall through - uses the fixed SMP URL instead
    }

    // For the dynamic part, only the recipient is needed
    return _getSMPClient (aRecipientID);
  }
}
