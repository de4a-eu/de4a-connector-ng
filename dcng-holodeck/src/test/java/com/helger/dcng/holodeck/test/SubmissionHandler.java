package com.helger.dcng.holodeck.test;

import java.net.URL;

import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.url.URLHelper;
import com.helger.dcng.api.me.MEException;
import com.helger.dcng.holodeck.EBMSUtils;

/**
 * An internal representation of a simple gateway that handles a submitted
 * message.
 *
 * Since it represents both c2 and c3 It does three things:
 *
 * 1. send back a submission result 2. send back a relay result 3. deliver a
 * message back to the backend
 *
 * @author myildiz
 */
public class SubmissionHandler {

  private static final Logger LOG = LoggerFactory.getLogger(SubmissionHandler.class);
  private static final URL BACKEND_URL = URLHelper.getAsURL("http://localhost:10001/backend");

  public static void handle(final SOAPMessage receivedMessage) {
    final Thread th = new Thread(() -> {
      try {
        // send back a submission result
        LOG.info("Handle submission for " + EBMSUtils.getMessageId(receivedMessage));

        LOG.info("Send back a submission result");
        final SOAPMessage submissionResult = DummyEBMSUtils.inferSubmissionResult(receivedMessage);

        EBMSUtils.sendSOAPMessage(submissionResult, BACKEND_URL);

        // wait a bit
        ThreadHelper.sleep(1000);

        LOG.info("Send back a relay result");
        final SOAPMessage relayResult = DummyEBMSUtils.inferRelayResult(receivedMessage);

        EBMSUtils.sendSOAPMessage(relayResult, BACKEND_URL);

        // wait a bit
        ThreadHelper.sleep(1000);

        // LOG.info("Send back a delivery message");
        // SOAPMessage deliveryMessage = TestEBMSUtils.inferDelivery(receivedMessage);
        // EBMSUtils.sendSOAPMessage(deliveryMessage, BACKEND_URL);

        // done
        LOG.info("DONE");
      } catch (final MEException ex) {
        LOG.error("Error processing in Thread", ex);
      }
    });
    th.setName("submission-handler");
    th.start();
  }
}
