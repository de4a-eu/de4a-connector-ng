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
package com.helger.rdc.api.user;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
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
import com.helger.rdc.api.RDCIdentifierFactory;
import com.helger.rdc.api.me.EMEProtocol;
import com.helger.rdc.api.rest.TCOutgoingMessage;
import com.helger.rdc.api.rest.TCOutgoingMetadata;
import com.helger.rdc.api.rest.TCPayload;
import com.helger.rdc.api.rest.RDCRestJAXB;

public class MainValidateLookupAndSendEdmRequest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainValidateLookupAndSendEdmRequest.class);

  public static void main (final String [] args) throws IOException
  {
    final TCOutgoingMessage aOM = new TCOutgoingMessage ();
    {
      final TCOutgoingMetadata aMetadata = new TCOutgoingMetadata ();
      aMetadata.setSenderID (RDCRestJAXB.createTCID (RDCIdentifierFactory.PARTICIPANT_SCHEME, "9914:tc-ng-test-sender"));
      aMetadata.setReceiverID (RDCRestJAXB.createTCID (RDCIdentifierFactory.PARTICIPANT_SCHEME, "9915:tooptest"));
      aMetadata.setDocTypeID (RDCRestJAXB.createTCID (RDCIdentifierFactory.DOCTYPE_SCHEME,
                                                     "urn:eu:toop:ns:dataexchange-1p40::Response##urn:eu.toop.response.registeredorganization::1.40"));
      aMetadata.setProcessID (RDCRestJAXB.createTCID (RDCIdentifierFactory.PROCESS_SCHEME, "urn:eu.toop.process.datarequestresponse"));
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aOM.setMetadata (aMetadata);
    }
    {
      final TCPayload aPayload = new TCPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource ("edm/Concept Request_LP.xml")));
      aPayload.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      aPayload.setContentID ("mock-request@toop");
      aOM.addPayload (aPayload);
    }

    LOGGER.info (RDCRestJAXB.outgoingMessage ().getAsString (aOM));

    try (HttpClientManager aHCM = new HttpClientManager ())
    {
      final HttpPost aPost = new HttpPost ("http://localhost:8090/api/user/submit/request");
      aPost.setEntity (new ByteArrayEntity (RDCRestJAXB.outgoingMessage ().getAsBytes (aOM)));
      final IJson aJson = aHCM.execute (aPost, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
