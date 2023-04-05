package com.helger.dcng.holodeck.test;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletHandler;

/**
 * @author yerlibilgin
 */
public class GWMocServletContainer
{
  private static Server server;

  public static void createServletOn (final int port, final String localPath)
  {
    final Thread t = new Thread ( () -> {
      try
      {
        server = new Server (port);

        final ServletHandler servletHandler = new ServletHandler ();
        server.setHandler (servletHandler);

        servletHandler.addServletWithMapping (SampleGWServlet.class, localPath).getServlet ();

        server.start ();
        server.join ();
      }
      catch (final Exception ex)
      {
        throw new IllegalStateException (ex.getMessage (), ex);
      }

    });
    t.setDaemon (true);
    t.start ();
  }

  public static void stop ()
  {
    try
    {
      server.stop ();
    }
    catch (final Exception ignored)
    {}
  }
}
