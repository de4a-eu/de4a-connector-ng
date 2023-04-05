package com.helger.dcng.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttp;
import com.helger.commons.http.EHttpMethod;
import com.helger.dcng.mockdp.MockDO;
import com.helger.xservlet.AbstractXServlet;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * The servlet to stop the DO. This is only required for Connectathons, to mimic
 * a non-responsive DO. To turn in on again, see {@link DcngServletDOUp}.
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
