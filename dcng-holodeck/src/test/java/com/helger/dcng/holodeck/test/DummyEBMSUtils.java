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
package com.helger.dcng.holodeck.test;

import java.nio.charset.StandardCharsets;

import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;

import com.helger.commons.io.stream.StreamHelper;
import com.helger.commons.io.stream.StringInputStream;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.me.incoming.MEIncomingException;
import com.helger.dcng.holodeck.DateTimeUtils;
import com.helger.dcng.holodeck.EBMSUtils;
import com.helger.dcng.holodeck.MEMConstants;
import com.helger.dcng.holodeck.SoapUtil;
import com.helger.dcng.holodeck.SoapXPathUtil;

/**
 * @author myildiz
 */
final class DummyEBMSUtils
{
  private static boolean failOnSubmissionResult;
  private static boolean failOnRelayResult;
  private static String relayEbmsError;

  private static final String xmlTemplate = StreamHelper.getAllBytesAsString (DummyEBMSUtils.class.getResourceAsStream ("/relay-sr-template.txt"),
                                                                              StandardCharsets.UTF_8);

  /*
   * Process the soap message and create a SubmissionResult from it
   */
  public static SOAPMessage inferSubmissionResult (final SOAPMessage receivedMessage) throws MEIncomingException
  {
    final String action = MEMConstants.ACTION_SUBMISSION_RESULT;

    String theAS4Message;
    try
    {
      theAS4Message = SoapXPathUtil.safeFindSingleNode (receivedMessage.getSOAPHeader (), ".//:Property[@name='MessageId']/text()")
                                   .getTextContent ();
    }
    catch (final SOAPException e)
    {
      throw new MEIncomingException (e.getMessage (), e);
    }

    String xml = xmlTemplate;
    xml = StringHelper.replaceAll (xml, "${timestamp}", DateTimeUtils.getCurrentTimestamp ());
    xml = StringHelper.replaceAll (xml, "${messageId}", EBMSUtils.genereateEbmsMessageId ("test"));
    xml = StringHelper.replaceAll (xml, "${action}", action);
    xml = StringHelper.replaceAll (xml, "${propMessageId}", theAS4Message);
    xml = StringHelper.replaceAll (xml, "${propRefToMessageId}", EBMSUtils.getMessageId (receivedMessage));

    if (failOnSubmissionResult)
    {
      xml = StringHelper.replaceAll (xml, "${result}", "Error");
      xml = StringHelper.replaceAll (xml, "${description}", "Shit happens!");
      xml = StringHelper.replaceAll (xml, "${errorCode}", "Shit happens!");
    }
    else
    {
      xml = StringHelper.replaceAll (xml, "${result}", "Receipt");
    }

    try
    {
      return SoapUtil.createMessage (null, new StringInputStream (xml, StandardCharsets.UTF_8));
    }
    catch (final Exception e)
    {
      throw new MEIncomingException (e.getMessage (), e);
    }
  }

  /*
   * Process the soap message and create a RelayResult from it
   */
  public static SOAPMessage inferRelayResult (final SOAPMessage receivedMessage) throws MEIncomingException
  {
    final String action = MEMConstants.ACTION_RELAY;

    String refToMessageId;
    try
    {
      refToMessageId = SoapXPathUtil.safeFindSingleNode (receivedMessage.getSOAPHeader (), ".//:Property[@name='MessageId']/text()")
                                    .getTextContent ();
    }
    catch (final SOAPException e)
    {
      throw new MEIncomingException (e.getMessage (), e);
    }

    String xml = xmlTemplate;
    xml = StringHelper.replaceAll (xml, "${timestamp}", DateTimeUtils.getCurrentTimestamp ());
    xml = StringHelper.replaceAll (xml, "${messageId}", EBMSUtils.genereateEbmsMessageId ("test"));
    xml = StringHelper.replaceAll (xml, "${action}", action);
    xml = StringHelper.replaceAll (xml, "${propMessageId}", refToMessageId);
    xml = StringHelper.replaceAll (xml, "${propRefToMessageId}", refToMessageId);

    if (failOnRelayResult)
    {
      xml = StringHelper.replaceAll (xml, "${result}", "Error");
      xml = StringHelper.replaceAll (xml, "${errorCode}", relayEbmsError);
    }
    else
    {
      xml = StringHelper.replaceAll (xml, "${result}", "Receipt");
    }

    try
    {
      return SoapUtil.createMessage (null, new StringInputStream (xml, StandardCharsets.UTF_8));
    }
    catch (final Exception e)
    {
      throw new MEIncomingException (e.getMessage (), e);
    }
  }

  /*
   * Process the soap message and create a Deliver message from it
   */
  public static SOAPMessage inferDelivery (final SOAPMessage receivedMessage)
  {
    return receivedMessage;
  }

  public static void setFailOnSubmissionResult (final boolean fail)
  {
    DummyEBMSUtils.failOnSubmissionResult = fail;
  }

  public static void setFailOnRelayResult (final boolean fail, final String errorCode)
  {
    DummyEBMSUtils.failOnRelayResult = fail;
    DummyEBMSUtils.relayEbmsError = errorCode;
  }
}
