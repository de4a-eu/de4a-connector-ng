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
