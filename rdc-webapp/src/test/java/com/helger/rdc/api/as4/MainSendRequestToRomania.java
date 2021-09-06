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

public class MainSendRequestToRomania
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendRequestToRomania.class);

  public static void main (final String [] args) throws IOException
  {
    final RDCOutgoingMessage aOM = new RDCOutgoingMessage ();
    {
      final RDCOutgoingMetadata aMetadata = new RDCOutgoingMetadata ();
      aMetadata.setSenderID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9915:de4atest"));
      aMetadata.setReceiverID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:ro000000006"));
      aMetadata.setDocTypeID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration"));
      aMetadata.setProcessID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PROCESS_SCHEME, "request"));
      // aMetadata.setPayloadType (RDCPayloadType.REQUEST);
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aMetadata.setEndpointURL ("https://de4a.onrc.ro:8444/de4a-connector/phase4");
      aMetadata.setReceiverCertificate (Base64.decode ("MIIFjzCCA3egAwIBAgICEAgwDQYJKoZIhvcNAQELBQAwajELMAkGA1UEBhMCRVUxDzANBgNVBAgMBkV1cm9wZTENMAsGA1UECgwEREU0QTElMCMGA1UECwwcREU0QSBXUDUgRGV2IEludGVybWVkaWF0ZSBDQTEUMBIGA1UEAwwLREU0YSBXUDUgSU0wHhcNMjEwNTI4MDkxNzI5WhcNMjYxMTE4MDkxNzI5WjBuMQswCQYDVQQGEwJFVTELMAkGA1UECAwCUk8xJzAlBgNVBAoMHk5hdGlvbmFsIFRyYWRlIFJlZ2lzdGVyIE9mZmljZTETMBEGA1UECwwKREU0QSBQaWxvdDEUMBIGA1UEAwwLYXM0Lm9ucmMucm8wggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQDAVRUM6rgk63HnU7l5WyBXgcajEDRBP3hhwrVbIYJIvjILrQ//Bl19PDuAAg43MO85hUEwPx5n9wE8l1qhv/H5PFt85iEO82wnSnEr5z5QxNIplbbtNpGti2WpWKrmReIhYYQ31vg6hFWA3Ilj9ZrNPu1qsKWLnJEOyoOb46SHGA20COF6S27cXdf2kFLmsOZYpVVYpZfOdN2+q4m1upZadXCKlrNfA60hkvkMzIBpTNSvyAD9vGp3DZS3fPIDAAa+Iwyrfve96vBb7IJ14BfdtpLcjbRmj2fg4DBsNK12sWn/ohBauosoOisNam2EKpWCopLAwG3QrP6ZEwtobavhAgMBAAGjggE5MIIBNTAJBgNVHRMEAjAAMBEGCWCGSAGG+EIBAQQEAwIGQDAzBglghkgBhvhCAQ0EJhYkT3BlblNTTCBHZW5lcmF0ZWQgU2VydmVyIENlcnRpZmljYXRlMB0GA1UdDgQWBBSotWc10hvIZoUB9jT8IsNIDEMAITCBmwYDVR0jBIGTMIGQgBSBcKEDa9DOx7ss9a78Mktcjaj2w6F0pHIwcDELMAkGA1UEBhMCRVUxDzANBgNVBAgMBkV1cm9wZTENMAsGA1UECgwEREU0QTErMCkGA1UECwwiREU0QSBXUDUgRGV2IENlcnRpZmljYXRlIEF1dGhvcml0eTEUMBIGA1UEAwwLREU0QSBXUDUgQ0GCAhAAMA4GA1UdDwEB/wQEAwIFoDATBgNVHSUEDDAKBggrBgEFBQcDATANBgkqhkiG9w0BAQsFAAOCAgEAWr7f+Xt4XM+pVAqyToXWiiExifrjRyesoVmzycbNfjaxAXUEiLV+zizx+1gJMDmpZl6joZfVuVYqS7wnMQsrVnfk6vG4nHDVUzmIFj/bR5yRoAnw+sRf8HxQFrB3nsBxkhlwZWiMGbVOc4LZDKiv9B3uPGscQBYgIXxQd7g9KMuKNBcbldZVd7TMoTSFIsJNK2KuVksndYYtFvRNGNSbprAJM5mIjGH0mVMaGdh2iL2tkrkfRWLXh05F4X9DBvRoKlNeAsusqn/3ftcOmmZz+nez5unRvo3Nwu3Dzbe8kYl/lLWXlQ2RSZA1kBrD/V1MlPvtvqR3dcmlX0jInClSH6IonDvksaJPwHqVI9LAwYE0ZkIaZ0C20ArfRBNfhaQ9rQuOTQnZV+R8aXddP2Y/Iw9P6H4BPkm7xRrO/IMRbjFQNBab+8KDpmdJLMmFOO9oAjnNPXPZ1liarMn4KGHvi6Gvhk9P6bgyKVbNoIVjq9lFOPdd/1fDYTmb+ElH8MRSYxSKTDOCKi8NMZr2McV48FJFr5xSDQXavW04hQvPtQxdOmAhSVHODGZSJK9cWweljvR2agLJLvZd6MPoTxeUENB9rM77lMyAWQqy6FXaxMSp5o5R/zLdlR0JgCkZ3KN3rErY+qv7YfnwKt59QyvkySOU5NQoWZ3l7yBYhEoyIeg="));
      aOM.setMetadata (aMetadata);
    }
    {
      final RDCPayload aPayload = new RDCPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource ("xml/dba-ro-1.xml")));
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
