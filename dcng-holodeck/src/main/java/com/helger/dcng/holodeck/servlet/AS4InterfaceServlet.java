package com.helger.dcng.holodeck.servlet;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.io.stream.NonBlockingByteArrayInputStream;
import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.me.incoming.MEIncomingException;
import com.helger.dcng.holodeck.EBMSUtils;
import com.helger.dcng.holodeck.MEMConstants;
import com.helger.dcng.holodeck.MEMDelegate;
import com.helger.dcng.holodeck.MEMDumper;
import com.helger.dcng.holodeck.SoapUtil;
import com.helger.dcng.holodeck.SoapXPathUtil;

/**
 * @author myildiz at 15.02.2018.
 */
@WebServlet ("/from-as4")
public class AS4InterfaceServlet extends HttpServlet
{

  private static final Logger LOGGER = LoggerFactory.getLogger (AS4InterfaceServlet.class);

  @Override
  protected void doPost (final HttpServletRequest req, final HttpServletResponse resp) throws IOException
  {
    LOGGER.info ("Received a mem 'external' message from the gateway");

    // Convert the request headers into MimeHeaders
    final MimeHeaders mimeHeaders = readMimeHeaders (req);

    // no matter what happens, we will return either a receipt or a fault
    resp.setContentType (CMimeType.TEXT_XML.getAsString ());

    SOAPMessage receivedMessage = null;
    try
    {
      final byte [] bytes = StreamHelper.getAllBytes (req.getInputStream ());
      if (LOGGER.isDebugEnabled ())
      {
        LOGGER.debug ("Read inbound message");
      }

      MEMDumper.dumpIncomingMessage (bytes);

      // Todo, remove buffering later
      try (final NonBlockingByteArrayInputStream is = new NonBlockingByteArrayInputStream (bytes))
      {
        receivedMessage = SoapUtil.createMessage (mimeHeaders, is);
      }

      // check if the message is a notification message

      if (LOGGER.isTraceEnabled ())
      {
        LOGGER.trace (SoapUtil.describe (receivedMessage));
      }

      // get the action from the soap message
      final String action = SoapXPathUtil.getSingleNodeTextContent (receivedMessage.getSOAPHeader (), "//:CollaborationInfo/:Action");

      switch (action)
      {
        case MEMConstants.ACTION_DELIVER:
          processDelivery (receivedMessage);
          break;

        case MEMConstants.ACTION_RELAY:
          processRelayResult (receivedMessage);
          break;

        // does not exist in the standard CIT interface.
        case MEMConstants.ACTION_SUBMISSION_RESULT:
          processSubmissionResult (receivedMessage);
          break;

        default:
          throw new UnsupportedOperationException ("Action '" + action + "' is not supported");
      }

      if (LOGGER.isDebugEnabled ())
      {
        LOGGER.debug ("Create success receipt");
      }
      final byte [] successReceipt = EBMSUtils.createSuccessReceipt (receivedMessage);

      if (LOGGER.isDebugEnabled ())
      {
        LOGGER.debug ("Send success receipt");
      }
      resp.setStatus (HttpServletResponse.SC_OK);
      resp.getOutputStream ().write (successReceipt);
      resp.getOutputStream ().flush ();

      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("Done processing inbound AS4 message");
    }
    catch (final IOException | SOAPException | MEIncomingException | RuntimeException ex)
    {
      LOGGER.error ("Error processing the message", ex);
      try
      {
        sendBackFault (resp, receivedMessage, ex);
      }
      catch (final IOException ex2)
      {
        LOGGER.error ("Exception in Exception handling", ex2);
      }
    }
    finally
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("End doPost");
    }

    // Don't close output stream
    // resp.getOutputStream().close();
  }

  /**
   * Create a fault message from the given input data and send it back to the
   * client
   *
   * @param resp
   *        HTTP Servlet response
   * @param receivedMessage
   *        Received SOAP message
   * @param th
   *        Exception that occurred
   * @throws IOException
   *         In case of IO error
   */
  protected void sendBackFault (final HttpServletResponse resp, final SOAPMessage receivedMessage, final Throwable th) throws IOException
  {
    LOGGER.error ("Failed to process incoming AS4 message", th);
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("Create fault");
    }
    byte [] fault;
    try
    {
      fault = EBMSUtils.createFault (receivedMessage, th.getMessage ());
    }
    catch (final MEIncomingException ex)
    {
      LOGGER.error ("Error in creating fault to send back", ex);
      fault = null;
    }
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("Write fault to the stream");
    }
    resp.setStatus (HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
    resp.getOutputStream ().write (fault);
    resp.getOutputStream ().flush ();
  }

  protected void processSubmissionResult (final SOAPMessage submissionResult) throws MEIncomingException
  {
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("------->> Received SubmissionResult <<-------");
      LOGGER.debug ("Dispatch SubmissionResult");
      LOGGER.debug ("\n" + SoapUtil.describe (submissionResult));
    }

    MEMDelegate.getInstance ().dispatchSubmissionResult (submissionResult);
  }

  protected void processRelayResult (final SOAPMessage notification) throws MEIncomingException
  {
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("------->> Received RelayResult <<-------");
      LOGGER.debug ("Dispatch notification");
      LOGGER.debug ("\n" + SoapUtil.describe (notification));
    }

    MEMDelegate.getInstance ().dispatchRelayResult (notification);
  }

  protected void processDelivery (final SOAPMessage receivedMessage) throws MEIncomingException
  {
    if (LOGGER.isDebugEnabled ())
    {
      LOGGER.debug ("------->> Received Delivery <<-------");
      LOGGER.debug ("Dispatch inbound message");
      LOGGER.debug ("\n" + SoapUtil.describe (receivedMessage));
    }

    MEMDelegate.getInstance ().dispatchInboundMessage (receivedMessage);
  }

  protected MimeHeaders readMimeHeaders (final HttpServletRequest req)
  {
    final MimeHeaders mimeHeaders = new MimeHeaders ();
    final Enumeration <String> headerNames = req.getHeaderNames ();
    while (headerNames.hasMoreElements ())
    {
      final String header = headerNames.nextElement ();
      final String reqHeader = req.getHeader (header);
      if (LOGGER.isDebugEnabled ())
      {
        LOGGER.debug ("HEADER " + header + " " + reqHeader);
      }
      mimeHeaders.addHeader (header, reqHeader);
    }
    return mimeHeaders;
  }
}
