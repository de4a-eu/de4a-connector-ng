package com.helger.dcng.jetty;

import javax.annotation.concurrent.Immutable;

import com.helger.photon.jetty.JettyStarter;

/**
 * Run as a standalone web application in Jetty on port 9092.<br>
 * http://localhost:9092/
 *
 * @author Philip Helger
 */
@Immutable
public final class RunInJettyDCNG
{
  public static void main (final String [] args) throws Exception
  {
    final JettyStarter js = new JettyStarter (RunInJettyDCNG.class).setPort (9092)
                                                                   .setStopPort (9093)
                                                                   .setSessionCookieName ("DCNG_SESSION")
                                                                   .setContainerIncludeJarPattern (JettyStarter.CONTAINER_INCLUDE_JAR_PATTERN_ALL);
    js.run ();
  }
}
