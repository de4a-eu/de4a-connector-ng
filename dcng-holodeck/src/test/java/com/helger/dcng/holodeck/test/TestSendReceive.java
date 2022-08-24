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
package com.helger.dcng.holodeck.test;

import static org.junit.Assert.assertSame;

import java.net.URL;
import java.nio.charset.StandardCharsets;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.url.URLHelper;
import com.helger.config.fallback.ConfigWithFallback;
import com.helger.config.source.res.ConfigurationSourceProperties;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.dcng.holodeck.EActingSide;
import com.helger.dcng.holodeck.MEMDelegate;
import com.helger.dcng.holodeck.MEMHolodeckConfig;
import com.helger.dcng.holodeck.notifications.IMessageHandler;
import com.helger.scope.mock.ScopeAwareTestSetup;

/**
 * This test suite tests the whole sending/receiving of a simple MEMessage by
 * mocking the as4 gateway
 *
 * @author myildiz at 16.02.2018.
 */
@FixMethodOrder
public class TestSendReceive
{
  static
  {
    DcngConfig.setConfig (new ConfigWithFallback (new ConfigurationSourceProperties (new ClassPathResource ("toop-connector.elonia.unitTest.properties"),
                                                                                     StandardCharsets.UTF_8)));
  }

  // this must be created after the above level setting statement
  private static final Logger LOG = LoggerFactory.getLogger (TestSendReceive.class);
  private static IMERoutingInformation gatewayRoutingMetadata;
  private static MEMessage sampleMessage;

  /**
   * Create a mock server on localhost that reads and sends back a MEMessage.
   */
  @BeforeClass
  public static void prepare ()
  {
    // Port must match the message-processor.properties
    if (LOG.isDebugEnabled ())
      LOG.debug ("Prepare for the test");

    final URL backendURL = URLHelper.getAsURL ("http://localhost:10001/backend");
    final URL gwURL = URLHelper.getAsURL (MEMHolodeckConfig.getMEMAS4Endpoint ());

    if (LOG.isInfoEnabled ())
    {
      LOG.info ("backend port: " + backendURL.getPort ());
      LOG.info ("backend localpath: " + backendURL.getPath ());
      LOG.info ("GW port: " + gwURL.getPort ());
      LOG.info ("GW localpath: " + gwURL.getPath ());
    }

    BackendServletContainer.createServletOn (backendURL.getPort (), backendURL.getPath ());
    GWMocServletContainer.createServletOn (gwURL.getPort (), gwURL.getPath ());
    ScopeAwareTestSetup.setupScopeTests ();
    gatewayRoutingMetadata = SampleDataProvider.createGatewayRoutingMetadata (EActingSide.DC,
                                                                              MEMHolodeckConfig.getMEMAS4Endpoint ());
    sampleMessage = SampleDataProvider.createSampleMessage ();
    final IMessageHandler handler = meMessage1 -> LOG.info ("hooray! I Got a message");
    MEMDelegate.getInstance ().registerMessageHandler (handler);

    ThreadHelper.sleep (1000);

  }

  @AfterClass
  public static void shutdown ()
  {
    ScopeAwareTestSetup.shutdownScopeTests ();

    BackendServletContainer.stop ();
    GWMocServletContainer.stop ();
  }

  @Test
  public void testSendReceive () throws MEOutgoingException
  {
    DummyEBMSUtils.setFailOnRelayResult (false, null);
    DummyEBMSUtils.setFailOnSubmissionResult (false);
    MEMDelegate.getInstance ().sendMessage (gatewayRoutingMetadata, sampleMessage);
  }

  @Test
  public void testME002 ()
  {
    DummyEBMSUtils.setFailOnRelayResult (false, null);
    DummyEBMSUtils.setFailOnSubmissionResult (true);
    try
    {
      MEMDelegate.getInstance ().sendMessage (gatewayRoutingMetadata, sampleMessage);
      throw new IllegalStateException ("An error must occur");
    }
    catch (final MEOutgoingException meException)
    {
      LOG.error (meException.getMessage (), meException);
      assertSame (EDcngErrorCode.ME_002, meException.getErrorCode ());
    }
  }

  @Test
  public void testME003 ()
  {
    DummyEBMSUtils.setFailOnSubmissionResult (false);
    DummyEBMSUtils.setFailOnRelayResult (true, "EBMS:0301");
    try
    {
      MEMDelegate.getInstance ().sendMessage (gatewayRoutingMetadata, sampleMessage);
      throw new IllegalStateException ("An error must occur");
    }
    catch (final MEOutgoingException meException)
    {
      LOG.error (meException.getMessage (), meException);
      assertSame (EDcngErrorCode.ME_003, meException.getErrorCode ());
    }

  }

  @Test
  public void testME004 ()
  {
    DummyEBMSUtils.setFailOnSubmissionResult (false);
    DummyEBMSUtils.setFailOnRelayResult (true, "EBMS:0345");
    try
    {
      MEMDelegate.getInstance ().sendMessage (gatewayRoutingMetadata, sampleMessage);
      throw new IllegalStateException ("An error must occur");
    }
    catch (final MEOutgoingException meException)
    {
      LOG.error (meException.getMessage (), meException);
      assertSame (EDcngErrorCode.ME_004, meException.getErrorCode ());
    }
  }

}
