package com.helger.dcng.holodeck;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.soap.AttachmentPart;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.MimeHeaders;
import javax.xml.soap.SOAPConnection;
import javax.xml.soap.SOAPConnectionFactory;
import javax.xml.soap.SOAPConstants;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Node;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.exception.InitializationException;
import com.helger.commons.io.stream.NonBlockingByteArrayOutputStream;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * @author myildiz at 12.02.2018.
 */
public class SoapUtil
{
  private static final MessageFactory MESSAGE_FACTORY;
  private static final SOAPConnectionFactory SOAP_CON_FACTORY;
  private static final DocumentBuilderFactory DBF = DocumentBuilderFactory.newInstance ();

  private static final Transformer SERIALIZER;

  static
  {
    try
    {
      // Ensure to use SOAP 1.2
      MESSAGE_FACTORY = MessageFactory.newInstance (SOAPConstants.SOAP_1_2_PROTOCOL);
      SOAP_CON_FACTORY = SOAPConnectionFactory.newInstance ();
      SERIALIZER = TransformerFactory.newInstance ().newTransformer ();
      SERIALIZER.setOutputProperty (OutputKeys.INDENT, "yes");
      SERIALIZER.setOutputProperty ("{http://xml.apache.org/xslt}indent-amount", "2");
    }
    catch (final Exception e)
    {
      throw new InitializationException ("Failed to initialize factories", e);
    }
    DBF.setNamespaceAware (true);
  }

  /**
   * Hidden constructor
   */
  private SoapUtil ()
  {}

  /**
   * A utility method to create a SOAP1.2 With Attachments message
   *
   * @return new {@link SOAPMessage}.
   * @throws MEOutgoingException
   *         in case of error
   */
  public static SOAPMessage createEmptyMessage () throws MEOutgoingException
  {
    try
    {
      return MESSAGE_FACTORY.createMessage ();
    }
    catch (final SOAPException e)
    {
      throw new MEOutgoingException ("Failed to create new SOAP message", e);
    }
  }

  /**
   * This method sends a SOAP1.2 message to the given url.
   *
   * @param message
   *        message to be send
   * @param endpoint
   *        endpoint to send the message to
   * @return The response message
   * @throws MEOutgoingException
   *         in case of error
   */
  public static SOAPMessage sendSOAPMessage (final SOAPMessage message, final URL endpoint) throws MEOutgoingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "Sending AS4 SOAP message to " + endpoint.toExternalForm ());
    MEMDumper.dumpOutgoingMessage (message);
    try
    {
      final SOAPConnection connection = SOAP_CON_FACTORY.createConnection ();
      return connection.call (message, endpoint);
    }
    catch (final SOAPException e)
    {
      throw new MEOutgoingException (EDcngErrorCode.ME_001, e);
    }
  }

  /**
   * Create a SOAP message from the provided mime headers and an input stream
   *
   * @param headers
   *        the MIME headers that will be used during the transportation as a
   *        HTTP package
   * @param is
   *        the input stream that the soap message has been serialized to
   *        previously
   * @return message
   * @throws IOException
   *         on IO error
   * @throws SOAPException
   *         on SOAP error
   */
  public static SOAPMessage createMessage (final MimeHeaders headers, final InputStream is) throws IOException, SOAPException
  {
    return MESSAGE_FACTORY.createMessage (headers, is);
  }

  /**
   * returns a String description of the provided soap message as XML appended
   * to an enumeration of the attachments and provides info such as id, type and
   * length
   *
   * @param message
   *        message
   * @return debug string
   */
  public static String describe (final SOAPMessage message)
  {
    final StringBuilder attSummary = new StringBuilder ();
    message.getAttachments ().forEachRemaining (att -> {
      final AttachmentPart ap = (AttachmentPart) att;
      attSummary.append ("ID: ").append (ap.getContentId ()).append ("\n");
      attSummary.append ("   TYPE: ").append (ap.getContentType ()).append ("\n");
      try
      {
        attSummary.append ("   LEN: ").append (ap.getRawContentBytes ().length).append ("\n");
      }
      catch (final SOAPException ex)
      {
        // Then we don't log it
      }
    });

    return prettyPrint (message.getSOAPPart ()) + "\n\n" + attSummary;
  }

  /**
   * Print the given org.w3c.dom.Node object in an indented XML format
   *
   * @param node
   *        the node to be serialized to XML
   * @return formatted XML
   */
  public static String prettyPrint (final Node node)
  {
    try (final NonBlockingByteArrayOutputStream aBAOS = new NonBlockingByteArrayOutputStream ())
    {
      final Source xmlSource = new DOMSource (node);
      final StreamResult res = new StreamResult (aBAOS);
      SERIALIZER.transform (xmlSource, res);
      SERIALIZER.reset ();
      return aBAOS.getAsString (StandardCharsets.UTF_8);
    }
    catch (final Exception e)
    {
      return node.getTextContent ();
    }
  }
}
