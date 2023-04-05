package com.helger.dcng.holodeck.test;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.me.MEException;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.holodeck.EBMSUtils;
import com.helger.dcng.holodeck.SubmissionMessageProperties;

public final class EBSMUtilsTest {
  private static final Logger LOG = LoggerFactory.getLogger(EBSMUtilsTest.class);

  @Test
  public void testFault() throws SOAPException, IOException, MEException {
    final SubmissionMessageProperties sd = new SubmissionMessageProperties();
    sd.conversationId = "EBSMUtilsTestConv";
    final MEMessage msg = MEMessage.builder()
                                   .payload(x -> x.mimeType(CMimeType.APPLICATION_XML)
                                                  .contentID("blafoo")
                                                  .data("<?xml version='1.0'?><root demo='true' />",
                                                        StandardCharsets.ISO_8859_1))
                                   .build();
    final SOAPMessage sm = EBMSUtils.convert2MEOutboundAS4Message(sd, msg);
    assertNotNull(sm);
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream()) {
      sm.writeTo(aBAOS);
      LOG.info(aBAOS.getAsString(StandardCharsets.UTF_8));
    }

    final byte[] aFault = EBMSUtils.createFault(sm, "Unit test fault");
    LOG.info(new String(aFault, StandardCharsets.UTF_8));
  }
}
