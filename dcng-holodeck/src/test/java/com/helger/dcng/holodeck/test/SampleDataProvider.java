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
package com.helger.dcng.holodeck.test;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MERoutingInformation;
import com.helger.dcng.holodeck.EActingSide;
import com.helger.peppol.smp.ESMPTransportProfile;
import com.helger.peppolid.factory.IIdentifierFactory;

/**
 * @author yerlibilgin
 */
public class SampleDataProvider
{

  private static KeyStore domibusKeystore;

  public static X509Certificate readDomibusCert (final String alias)
  {
    try
    {
      if (domibusKeystore == null)
      {
        // multithread initialiation danger... yes no big deal.
        domibusKeystore = KeyStore.getInstance ("JKS");
        domibusKeystore.load (SampleDataProvider.class.getResourceAsStream ("/dev-gw-jks/domibus-toop-keys.jks"), "test123".toCharArray ());
      }

      return (X509Certificate) domibusKeystore.getCertificate (alias);

    }
    catch (final Exception e)
    {
      throw new IllegalStateException (e);
    }
  }

  @Nonnull
  public static X509Certificate readCert (final EActingSide actingSide)
  {
    try
    {
      // If I am DC, use dp certificate or vice versa
      final String certName = actingSide == EActingSide.DC ? "/freedonia.crt" : "/elonia.crt";
      return (X509Certificate) CertificateFactory.getInstance ("X509")
                                                 .generateCertificate (SampleDataProvider.class.getResourceAsStream (certName));
    }
    catch (final CertificateException e)
    {
      throw new IllegalStateException (e);
    }
  }

  public static IMERoutingInformation createGatewayRoutingMetadata (final EActingSide actingSide, final String receivingGWURL)
  {
    final X509Certificate aCert = readCert (actingSide);
    return createGatewayRoutingMetadata (receivingGWURL, aCert);
  }

  public static IMERoutingInformation createGatewayRoutingMetadata (final String targetURL, final X509Certificate targetCert)
  {
    final IIdentifierFactory aIF = DcngConfig.getIdentifierFactory ();
    final IMERoutingInformation metadata = new MERoutingInformation (aIF.createParticipantIdentifier (DcngIdentifierFactory.PARTICIPANT_SCHEME,
                                                                                                      "0088:123456"),
                                                                     aIF.createParticipantIdentifier (DcngIdentifierFactory.PARTICIPANT_SCHEME,
                                                                                                      "0099:123456"),
                                                                     aIF.createDocumentTypeIdentifier (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                                                                       "CompanyRegistration:1.0"),
                                                                     aIF.createProcessIdentifier (DcngIdentifierFactory.PROCESS_SCHEME,
                                                                                                  "request"),
                                                                     ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4.getID (),
                                                                     targetURL,
                                                                     targetCert);

    return metadata;
  }

  @Nonnull
  public static MEMessage createSampleMessage ()
  {
    return MEMessage.builder ()
                    .payload (x -> x.mimeType (CMimeType.APPLICATION_XML)
                                    .contentID ("xmlpayload@dp")
                                    .data ("<sample>that is a sample xml</sample>", StandardCharsets.ISO_8859_1))
                    .build ();
  }
}
