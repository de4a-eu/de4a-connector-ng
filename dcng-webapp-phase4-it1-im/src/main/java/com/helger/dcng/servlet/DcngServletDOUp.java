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
package com.helger.dcng.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.dcng.mockdp.MockDO;
import com.helger.xservlet.AbstractXServlet;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * The servlet to start the DO. This is only required for Connectathons, to
 * mimic a responsive DO. To turn in off, see {@link DcngServletDODown}.
 *
 * @author Philip Helger
 */
@WebServlet ("/do-up/*")
public class DcngServletDOUp extends AbstractXServlet
{
  public static final String SERVLET_DEFAULT_NAME = "do-down";
  public static final String SERVLET_DEFAULT_PATH = '/' + SERVLET_DEFAULT_NAME;

  public DcngServletDOUp ()
  {
    handlerRegistry ().registerHandler (EHttpMethod.GET, (aRequestScope, aUnifiedResponse) -> {
      DE4AKafkaClient.send (EErrorLevel.INFO, "Starting DO");
      MockDO.DO_ACTIVE.set (true);
      aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT);
    });
  }
}
