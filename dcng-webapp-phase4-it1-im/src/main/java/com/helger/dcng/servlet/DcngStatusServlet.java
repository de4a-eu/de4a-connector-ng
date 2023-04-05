package com.helger.dcng.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.http.EHttpMethod;
import com.helger.xservlet.AbstractXServlet;

/**
 * The servlet to show the application status.
 *
 * @author Philip Helger
 */
@WebServlet ("/status/*")
public class DcngStatusServlet extends AbstractXServlet
{
  public static final String SERVLET_DEFAULT_NAME = "status";
  public static final String SERVLET_DEFAULT_PATH = '/' + SERVLET_DEFAULT_NAME;

  public DcngStatusServlet ()
  {
    handlerRegistry ().registerHandler (EHttpMethod.GET, new DcngStatusXServletHandler ());
  }
}
