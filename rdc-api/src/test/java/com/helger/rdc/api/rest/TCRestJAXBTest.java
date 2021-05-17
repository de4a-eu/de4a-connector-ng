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
package com.helger.rdc.api.rest;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.nio.charset.StandardCharsets;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;

import com.helger.commons.io.resource.FileSystemResource;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.mock.CommonsTestHelper;
import com.helger.rdc.api.TCIdentifierFactory;
import com.helger.rdc.api.me.EMEProtocol;

/**
 * Test class for class {@link TCRestJAXB}.
 *
 * @author Philip Helger
 */
public final class TCRestJAXBTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (TCRestJAXBTest.class);

  @Test
  public void testBasic ()
  {
    assertTrue (TCRestJAXB.XSD_RES.exists ());
  }

  @Test
  public void testOutgoing ()
  {
    final TCOutgoingMessage m = new TCOutgoingMessage ();
    final TCOutgoingMetadata md = new TCOutgoingMetadata ();
    md.setSenderID (TCRestJAXB.createTCID (TCIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (TCRestJAXB.createTCID (TCIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (TCRestJAXB.createTCID (TCIdentifierFactory.DOCTYPE_SCHEME,
                                            "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.registeredorganization::1.40"));
    md.setProcessID (TCRestJAXB.createTCID (TCIdentifierFactory.DOCTYPE_SCHEME, "urn:eu.toop.process.datarequestresponse"));
    md.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
    md.setEndpointURL ("https://target.example.org/as4");
    md.setReceiverCertificate ("Receiver's certificate".getBytes (StandardCharsets.ISO_8859_1));
    m.setMetadata (md);

    final TCPayload p = new TCPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (TCRestJAXB.outgoingMessage ().getAsString (m));

    // Write
    final Document aDoc = TCRestJAXB.outgoingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final TCOutgoingMessage m2 = TCRestJAXB.outgoingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);

    // Read
    final TCOutgoingMessage m3 = TCRestJAXB.outgoingMessage ()
                                           .read (new FileSystemResource (new File ("src/test/resources/xml/rest1.xml")));
    assertNotNull (m3);
  }

  @Test
  public void testIncoming ()
  {
    final TCIncomingMessage m = new TCIncomingMessage ();
    final TCIncomingMetadata md = new TCIncomingMetadata ();
    md.setSenderID (TCRestJAXB.createTCID (TCIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (TCRestJAXB.createTCID (TCIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (TCRestJAXB.createTCID (TCIdentifierFactory.DOCTYPE_SCHEME,
                                            "urn:eu:toop:ns:dataexchange-1p40::Request##urn:eu.toop.request.registeredorganization::1.40"));
    md.setProcessID (TCRestJAXB.createTCID (TCIdentifierFactory.DOCTYPE_SCHEME, "urn:eu.toop.process.datarequestresponse"));
    md.setPayloadType (TCPayloadType.REQUEST);
    m.setMetadata (md);

    final TCPayload p = new TCPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (TCRestJAXB.incomingMessage ().getAsString (m));

    // Write
    final Document aDoc = TCRestJAXB.incomingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final TCIncomingMessage m2 = TCRestJAXB.incomingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);
  }
}
