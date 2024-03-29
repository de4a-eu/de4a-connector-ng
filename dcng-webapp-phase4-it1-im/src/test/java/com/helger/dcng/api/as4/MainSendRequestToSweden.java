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
package com.helger.dcng.api.as4;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.ByteArrayEntity;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.dcng.api.me.EMEProtocol;
import com.helger.dcng.api.rest.DCNGOutgoingMessage;
import com.helger.dcng.api.rest.DCNGOutgoingMetadata;
import com.helger.dcng.api.rest.DCNGPayload;
import com.helger.dcng.api.rest.DcngRestJAXB;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonWriter;
import com.helger.json.serialize.JsonWriterSettings;

public final class MainSendRequestToSweden
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendRequestToSweden.class);

  public static void main (final String [] args) throws IOException
  {
    final DCNGOutgoingMessage aOM = new DCNGOutgoingMessage ();
    {
      final DCNGOutgoingMetadata aMetadata = new DCNGOutgoingMetadata ();
      aMetadata.setSenderID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9915:de4atest"));
      aMetadata.setReceiverID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME,
                                                          "9991:se000000013"));
      aMetadata.setDocTypeID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE,
                                                         "CompanyRegistration"));
      aMetadata.setProcessID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PROCESS_SCHEME, "request"));
      // aMetadata.setPayloadType (DCNGPayloadType.REQUEST);
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aOM.setMetadata (aMetadata);
    }
    {
      final DCNGPayload aPayload = new DCNGPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource ("xml/dba-se-1.xml")));
      if (aPayload.getValue () == null || aPayload.getValue ().length == 0)
        throw new IllegalStateException ("Payload is empty");
      aPayload.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      aPayload.setContentID ("RequestTransferEvidence");
      aOM.addPayload (aPayload);
    }

    LOGGER.info (DcngRestJAXB.outgoingMessage ().getAsString (aOM));

    final HttpClientSettings aHCS = new HttpClientSettings ().setResponseTimeout (Timeout.ofSeconds (30));
    try (final HttpClientManager aHCM = HttpClientManager.create (aHCS))
    {
      final HttpPost aPost = new HttpPost ("http://localhost:9092/api/lookup/send");
      aPost.setEntity (new ByteArrayEntity (DcngRestJAXB.outgoingMessage ().getAsBytes (aOM),
                                            ContentType.APPLICATION_XML));
      final IJson aJson = aHCM.execute (aPost, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
