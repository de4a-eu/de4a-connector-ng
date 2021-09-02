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
import com.helger.rdc.api.RdcIdentifierFactory;
import com.helger.rdc.api.me.EMEProtocol;

/**
 * Test class for class {@link RdcRestJAXB}.
 *
 * @author Philip Helger
 */
public final class RdcRestJAXBTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RdcRestJAXBTest.class);

  @Test
  public void testBasic ()
  {
    assertTrue (RdcRestJAXB.XSD_RES.exists ());
  }

  @Test
  public void testOutgoing ()
  {
    final RDCOutgoingMessage m = new RDCOutgoingMessage ();
    final RDCOutgoingMetadata md = new RDCOutgoingMetadata ();
    md.setSenderID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration:1.0"));
    md.setProcessID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PROCESS_SCHEME, "request"));
    md.setPayloadType (RDCPayloadType.REQUEST);
    md.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
    md.setEndpointURL ("https://target.example.org/as4");
    md.setReceiverCertificate ("Receiver's certificate".getBytes (StandardCharsets.ISO_8859_1));
    m.setMetadata (md);

    final RDCPayload p = new RDCPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (RdcRestJAXB.outgoingMessage ().getAsString (m));

    // Write
    final Document aDoc = RdcRestJAXB.outgoingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final RDCOutgoingMessage m2 = RdcRestJAXB.outgoingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);

    // Read
    final RDCOutgoingMessage m3 = RdcRestJAXB.outgoingMessage ().read (new File ("src/test/resources/xml/rdc-outgoing.xml"));
    assertNotNull (m3);
    final RDCOutgoingMessage m4 = RdcRestJAXB.outgoingMessage ().read (new File ("src/test/resources/xml/rdc-outgoing2.xml"));
    assertNotNull (m4);
  }

  @Test
  public void testIncoming ()
  {
    final RDCIncomingMessage m = new RDCIncomingMessage ();
    final RDCIncomingMetadata md = new RDCIncomingMetadata ();
    md.setSenderID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration:1.0"));
    md.setProcessID (RdcRestJAXB.createRDCID (RdcIdentifierFactory.PROCESS_SCHEME, "request"));
    md.setPayloadType (RDCPayloadType.REQUEST);
    m.setMetadata (md);

    final RDCPayload p = new RDCPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (RdcRestJAXB.incomingMessage ().getAsString (m));

    // Write
    final Document aDoc = RdcRestJAXB.incomingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final RDCIncomingMessage m2 = RdcRestJAXB.incomingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);

    // Read
    final RDCOutgoingMessage m3 = RdcRestJAXB.outgoingMessage ()
                                             .read (new FileSystemResource (new File ("src/test/resources/xml/rdc-incoming.xml")));
    assertNotNull (m3);
  }
}
