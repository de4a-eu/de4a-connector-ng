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
package com.helger.dcng.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.dcng.mockdp.MockDO;
import com.helger.xservlet.AbstractXServlet;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * The servlet to stop the DO.
 *
 * @author Philip Helger
 */
@WebServlet ("/do-down/*")
public class DcngServletDODown extends AbstractXServlet
{
  public static final String SERVLET_DEFAULT_NAME = "do-down";
  public static final String SERVLET_DEFAULT_PATH = '/' + SERVLET_DEFAULT_NAME;

  public DcngServletDODown ()
  {
    handlerRegistry ().registerHandler (EHttpMethod.GET, (aRequestScope, aUnifiedResponse) -> {
      DE4AKafkaClient.send (EErrorLevel.INFO, "Shutting down DO");
      MockDO.DO_ACTIVE.set (false);
      aUnifiedResponse.setStatus (CHttp.HTTP_NO_CONTENT);
    });
  }
}
