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
package com.helger.rdc.api.as4;

import java.io.IOException;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.CGlobal;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.HttpClientSettings;
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

public final class MainSendRequestToSweden
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendRequestToSweden.class);

  public static void main (final String [] args) throws IOException
  {
    final RDCOutgoingMessage aOM = new RDCOutgoingMessage ();
    {
      final RDCOutgoingMetadata aMetadata = new RDCOutgoingMetadata ();
      aMetadata.setSenderID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9915:de4atest"));
      aMetadata.setReceiverID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9991:se000000013"));
      aMetadata.setDocTypeID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration"));
      aMetadata.setProcessID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PROCESS_SCHEME, "request"));
      // aMetadata.setPayloadType (RDCPayloadType.REQUEST);
      aMetadata.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
      aOM.setMetadata (aMetadata);
    }
    {
      final RDCPayload aPayload = new RDCPayload ();
      aPayload.setValue (StreamHelper.getAllBytes (new ClassPathResource ("xml/dba-se-1.xml")));
      if (aPayload.getValue () == null || aPayload.getValue ().length == 0)
        throw new IllegalStateException ("Payload is empty");
      aPayload.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
      aPayload.setContentID ("RequestTransferEvidence");
      aOM.addPayload (aPayload);
    }

    LOGGER.info (RdcRestJAXB.outgoingMessage ().getAsString (aOM));

    try (final HttpClientManager aHCM = HttpClientManager.create (new HttpClientSettings ().setSocketTimeoutMS ((int) (30 *
                                                                                                                       CGlobal.MILLISECONDS_PER_SECOND))))
    {
      final HttpPost aPost = new HttpPost ("http://localhost:9092/api/lookup/send");
      aPost.setEntity (new ByteArrayEntity (RdcRestJAXB.outgoingMessage ().getAsBytes (aOM)));
      final IJson aJson = aHCM.execute (aPost, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
