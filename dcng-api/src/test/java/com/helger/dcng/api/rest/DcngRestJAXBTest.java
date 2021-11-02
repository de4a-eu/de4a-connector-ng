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
package com.helger.dcng.api.rest;

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
import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.dcng.api.me.EMEProtocol;

/**
 * Test class for class {@link RdcRestJAXB}.
 *
 * @author Philip Helger
 */
public final class DcngRestJAXBTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngRestJAXBTest.class);

  @Test
  public void testBasic ()
  {
    assertTrue (DcngRestJAXB.XSD_RES.exists ());
  }

  @Test
  public void testOutgoing ()
  {
    final DCNGOutgoingMessage m = new DCNGOutgoingMessage ();
    final DCNGOutgoingMetadata md = new DCNGOutgoingMetadata ();
    md.setSenderID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration:1.0"));
    md.setProcessID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PROCESS_SCHEME, "request"));
    md.setTransportProtocol (EMEProtocol.AS4.getTransportProfileID ());
    md.setEndpointURL ("https://target.example.org/as4");
    md.setReceiverCertificate ("Receiver's certificate".getBytes (StandardCharsets.ISO_8859_1));
    m.setMetadata (md);

    final DCNGPayload p = new DCNGPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (DcngRestJAXB.outgoingMessage ().getAsString (m));

    // Write
    final Document aDoc = DcngRestJAXB.outgoingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final DCNGOutgoingMessage m2 = DcngRestJAXB.outgoingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);

    // Read
    final DCNGOutgoingMessage m3 = DcngRestJAXB.outgoingMessage ().read (new File ("src/test/resources/xml/dcng-outgoing.xml"));
    assertNotNull (m3);
    final DCNGOutgoingMessage m4 = DcngRestJAXB.outgoingMessage ().read (new File ("src/test/resources/xml/dcng-outgoing2.xml"));
    assertNotNull (m4);
  }

  @Test
  public void testIncoming ()
  {
    final DCNGIncomingMessage m = new DCNGIncomingMessage ();
    final DCNGIncomingMetadata md = new DCNGIncomingMetadata ();
    md.setSenderID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:sender"));
    md.setReceiverID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:receiver"));
    md.setDocTypeID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.DOCTYPE_SCHEME, "CompanyRegistration:1.0"));
    md.setProcessID (DcngRestJAXB.createDCNGID (DcngIdentifierFactory.PROCESS_SCHEME, "request"));
    m.setMetadata (md);

    final DCNGPayload p = new DCNGPayload ();
    p.setContentID ("cid1");
    p.setMimeType (CMimeType.APPLICATION_XML.getAsString ());
    p.setValue ("Hello World".getBytes (StandardCharsets.ISO_8859_1));
    m.addPayload (p);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m.clone ());

    if (false)
      LOGGER.info (DcngRestJAXB.incomingMessage ().getAsString (m));

    // Write
    final Document aDoc = DcngRestJAXB.incomingMessage ().getAsDocument (m);
    assertNotNull (aDoc);

    // Read
    final DCNGIncomingMessage m2 = DcngRestJAXB.incomingMessage ().read (aDoc);
    assertNotNull (m2);

    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, m2);

    // Read
    final DCNGOutgoingMessage m3 = DcngRestJAXB.outgoingMessage ()
                                               .read (new FileSystemResource (new File ("src/test/resources/xml/dcng-incoming.xml")));
    assertNotNull (m3);
  }
}
