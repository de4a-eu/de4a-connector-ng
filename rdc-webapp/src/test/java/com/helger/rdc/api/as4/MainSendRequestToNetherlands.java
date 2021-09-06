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
package com.helger.rdc.api.as4;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.bouncycastle.util.encoders.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonWriter;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.rdc.api.RdcIdentifierFactory;
import com.helger.rdc.api.me.EMEProtocol;
import com.helger.rdc.api.rest.RDCOutgoingMessage;
import com.helger.rdc.api.rest.RDCOutgoingMetadata;
import com.helger.rdc.api.rest.RDCPayload;
import com.helger.rdc.api.rest.RdcRestJAXB;

public final class MainSendRequestToNetherlands
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendRequestToNetherlands.class);

  public static void main (final String [] args) throws IOException
  {
    final RDCOutgoingMessage aOM = new RDCOutgoingMessage ();
    {
      final RDCOutgoingMetadata aMetadata = new RDCOutgoingMetadata ();
      aMetadata.setSenderID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9915:de4atest"));
      aMetadata.setReceiverID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:nl990000106"));
      aMetadata.setDocTypeID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration"));
      aMetadata.setProcessID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PROCESS_SCHEME, "request"));
      // aMetadata.setPayloadType (RDCPayloadType.REQUEST);
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aMetadata.setEndpointURL ("https://acc-eidas.minez.nl/de4a-connector/phase4");
      aMetadata.setReceiverCertificate (Base64.decode ("MIIFvzCCA6egAwIBAgICEAQwDQYJKoZIhvcNAQELBQAwajELMAkGA1UEBhMCRVUxDzANBgNVBAgMBkV1cm9wZTENMAsGA1UECgwEREU0QTElMCMGA1UECwwcREU0QSBXUDUgRGV2IEludGVybWVkaWF0ZSBDQTEUMBIGA1UEAwwLREU0YSBXUDUgSU0wHhcNMjEwNDI2MDgyODMyWhcNMjYxMDE3MDgyODMyWjCBnTELMAkGA1UEBhMCRVUxCzAJBgNVBAgMAk5MMS8wLQYDVQQKDCZSaWprc2RpZW5zdCB2b29yIE9uZGVybmVtZW5kIE5lZGVybGFuZDE4MDYGA1UECwwvQXV0aGVudGljYXRpb24gZm9yIGludGVybmF0aW9uYWwgcGlsb3QgcHJvamVjdHMxFjAUBgNVBAMMDWRlbm5pcy5ydm8ubmwwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDxSWoPBQr+cUPKwieIMGyZjaaZ2GU+5MCZF9stW9D1kztrPSW+659XLaZKi1V94yWbJa5sk/X3j46V9bF2bE+eDElyMFm1VfhbpkPqLaf05ize/4h8CyMDcjnPSUp7EzVZjLfvZGMTbJOMUSS03maaIuJvov4ME2GaVPpHjIDsbYf8AqmyFgpxRJzRQdGBO7ngirK6AjlkWzgyOJiX1HpMR5MU2u8he6ABQx2WcposV8Llz/cShJ5YVT68pZDrLqr5Rg+B23qiBSNihBqu5yLcrq1e0L3fdEqnNeXNBiyhvTGxzbbr5oLT5xGvcddcax1eDKhsrhv89ox4zpb0A2N3AgMBAAGjggE5MIIBNTAJBgNVHRMEAjAAMBEGCWCGSAGG+EIBAQQEAwIGQDAzBglghkgBhvhCAQ0EJhYkT3BlblNTTCBHZW5lcmF0ZWQgU2VydmVyIENlcnRpZmljYXRlMB0GA1UdDgQWBBQ8EjRG1ngGH9Bdh1yFgXS+19g33DCBmwYDVR0jBIGTMIGQgBSBcKEDa9DOx7ss9a78Mktcjaj2w6F0pHIwcDELMAkGA1UEBhMCRVUxDzANBgNVBAgMBkV1cm9wZTENMAsGA1UECgwEREU0QTErMCkGA1UECwwiREU0QSBXUDUgRGV2IENlcnRpZmljYXRlIEF1dGhvcml0eTEUMBIGA1UEAwwLREU0QSBXUDUgQ0GCAhAAMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATANBgkqhkiG9w0BAQsFAAOCAgEATQlgh2jJgV4eJb+qyZ6pi/2Ci4KGpHfpb/xAZUH5riTLgkyYf/j7BVIvwjUclc9GAItSRGb6uBC+gFs0Fx5lSXGZXmyr5gozwgpGeDysADmUc6/kNidhI76AgXCbIz9ULBcfFF10jgj12urEBI594KHCbuhteMLtbnokFKzaOwDlasU7DnH00m+SZWBgmLQjceJsCYgjLYUcFM9Uxo2a0kwyDyhWmcVBnJzSqF4EyXHybrm64Z2P6EbVafTnKKOSLUQr+qVYYmQ3jBlmnSCsd7KJAR5bzZhYl9QG4W0TQdMypXp0+RHc1lw7M8rYnh7T2Gg4thQRGm84fhpnHSZqHNoP09kqR7sTDyVBQLA/YHY1QQModbiF2m5V7WSuGBYi+MZyHIoJCf4ejUBXWHYM+GweejFM9PQfxec3/f/avOojBjn2Cd+Gcu8oqh8AleIP8LDYryrmLqxcFG07t9mQHIP6jxUFavs3/FUvNdGN2bKPEbGzxJ3NXmKot/Jm1rYbdiRQBXB4IqTbs0t42PId6NWBjviuFmr9spM9R5mfj+DbYpC2CfyNKXPYwQLWpJdhXhdgeKAY9JAH7aiHtCBc4askYQh/PsAtzGMnnaOKl8Cr1Gxz012Pf3NtbA38uwX6+q9lnFnIr1c6zxVldTCuotsmYPM1Yh9KzqOqk3OFStM="));
      aOM.setMetadata (aMetadata);
    }
    {
      final RDCPayload aPayload = new RDCPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource ("xml/dba-nl-1.xml")));
      aPayload.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      aPayload.setContentID ("mock-request@de4a");
      aOM.addPayload (aPayload);
    }

    LOGGER.info (RdcRestJAXB.outgoingMessage ().getAsString (aOM));

    try (final HttpClientManager aHCM = new HttpClientManager ())
    {
      final HttpPost aPost = new HttpPost ("http://localhost:8090/api/send");
      aPost.setEntity (new ByteArrayEntity (RdcRestJAXB.outgoingMessage ().getAsBytes (aOM)));
      final IJson aJson = aHCM.execute (aPost, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
