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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.concurrent.ThreadHelper;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.config.Config;
import com.helger.config.source.res.ConfigurationSourceProperties;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.holodeck.EActingSide;
import com.helger.dcng.holodeck.MEMDelegate;
import com.helger.dcng.holodeck.MEMHolodeckConfig;
import com.helger.dcng.holodeck.notifications.IMessageHandler;
import com.helger.scope.mock.ScopeAwareTestSetup;

/**
 * This class runs an and to end integration test with a locally running Toop
 * Compatible gateway. The simulation is from elonia-gw-gw-freedonia and back
 * <p>
 * This is not a unit test that is why it has a main method
 *
 * @author myildiz83
 */
public class IntegrationTestMain
{
  static
  {
    DcngConfig.setConfig (Config.create (new ConfigurationSourceProperties (new ClassPathResource ("toop-connector.elonia.integrationTest.properties"),
                                                                            StandardCharsets.UTF_8)));
    PropertyConfigurator.configure (IntegrationTestMain.class.getResourceAsStream ("/log4j.properties"));
  }

  private static final Logger LOG = LoggerFactory.getLogger (IntegrationTestMain.class);

  public static void main (final String [] args) throws IOException
  {

    ScopeAwareTestSetup.setupScopeTests ();
    // initialize c1
    LOG.info ("Initialize corner1");
    BackendServletContainer.createServletOn (8585, "/msh");
    // initialize c4
    LOG.info ("Initialize corner4");
    BackendServletContainer.createServletOn (8686, "/msh");

    final BufferedReader bufferedReader = new BufferedReader (new InputStreamReader (System.in));

    // wait for the other side to receive the message
    final DeliveryWatcher deliveryWatcher = new DeliveryWatcher ();
    MEMDelegate.getInstance ().registerMessageHandler (deliveryWatcher);

    ThreadHelper.sleep (1000);

    // the URL of both C2 and C3 were set to the same endpoint
    // for this test.
    final String recivingSideURL = MEMHolodeckConfig.getMEMAS4Endpoint ();

    String command;
    inputLoop: while ((command = waitForUserInput (bufferedReader)) != null)
    {
      switch (command)
      {
        case "e2f":
          updateConfig (command);
          sendMessage (deliveryWatcher, EActingSide.DC, recivingSideURL);
          break;

        case "f2e":
          updateConfig (command);
          sendMessage (deliveryWatcher, EActingSide.DP, recivingSideURL);
          break;

        case "q":
          LOG.info ("Exit gracefully");
          break inputLoop;

        default:
          break;
      }

      LOG.info ("");
    }
  }

  private static void updateConfig (final String line)
  {
    String targetConfiguration = "toop-connector.elonia.integrationTest.properties";

    switch (line)
    {
      case "e2f":
        targetConfiguration = "toop-connector.elonia.integrationTest.properties";
        break;

      case "f2e":
        targetConfiguration = "toop-connector.freedonia.integrationTest.properties";
        break;
    }

    DcngConfig.setConfig (Config.create (new ConfigurationSourceProperties (new ClassPathResource (targetConfiguration),
                                                                            StandardCharsets.UTF_8)));
  }

  /**
   * Send a message from one side to the other
   */
  private static void sendMessage (final DeliveryWatcher deliveryWatcher, final EActingSide actingSide, final String recivingSideURL)
  {
    deliveryWatcher.reset ();

    // set the address of the receiving gateway to t
    final IMERoutingInformation gatewayRoutingMetadata = SampleDataProvider.createGatewayRoutingMetadata (actingSide, recivingSideURL);
    final MEMessage meMessage = SampleDataProvider.createSampleMessage ();

    try
    {
      MEMDelegate.getInstance ().sendMessage (gatewayRoutingMetadata, meMessage);
    }
    catch (final Throwable th)
    {

      LOG.info ("TEST for " + actingSide + " FAILED");
      LOG.error ("Failed to send a message");
      LOG.error (th.getMessage (), th);
      return;
    }

    LOG.info ("Wait for the delivery");
    final MEMessage obtainedMessage = deliveryWatcher.obtainMessage (60, TimeUnit.SECONDS);
    if (obtainedMessage == null)
    {
      LOG.error ("Failed to receive a message from the other side within 20 seconds timeout");
      LOG.info ("TEST for " + actingSide + " FAILED");
    }
    else
    {
      LOG.info ("The message was successfully received from the other side");
      LOG.info ("TEST for " + actingSide + " SUCCESSFUL");
    }
  }

  private static String waitForUserInput (final BufferedReader bufferedReader) throws IOException
  {
    LOG.info ("Wait for user input.");
    LOG.info ("e2f: send a message from elonia to freedonia");
    LOG.info ("f2e: send a message from freedonia to elonia");
    return bufferedReader.readLine ();
  }

  static class DeliveryWatcher implements IMessageHandler
  {
    private CountDownLatch latch = new CountDownLatch (1);
    private volatile MEMessage m_aMessage;

    public void reset ()
    {
      latch = new CountDownLatch (1);
      m_aMessage = null;
    }

    /**
     * implement this method to receive messages when an inbound message arrives
     * to the AS4 endpoint
     *
     * @param meMessage
     *        the object that contains the payloads and their metadata´
     * @throws Exception
     *         in case of error
     */
    @Override
    public void handleMessage (@Nonnull final MEMessage meMessage) throws Exception
    {
      synchronized (this)
      {
        this.m_aMessage = meMessage;
      }

      latch.countDown ();
    }

    public MEMessage obtainMessage (final long timeout, final TimeUnit timeUnit)
    {

      if (this.m_aMessage != null)
      {
        return m_aMessage;
      }

      try
      {
        latch.await (timeout, timeUnit);
      }
      catch (final InterruptedException e)
      {
        LOG.error ("wait interrupted", e);
      }
      synchronized (this)
      {
        try
        {
          return m_aMessage;
        }
        finally
        {
          m_aMessage = null;
        }
      }
    }
  }
}
