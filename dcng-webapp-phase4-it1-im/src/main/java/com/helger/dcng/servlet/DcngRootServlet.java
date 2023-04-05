package com.helger.dcng.servlet;

import java.io.IOException;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.mime.CMimeType;
import com.helger.dcng.api.me.IMessageExchangeSPI;
import com.helger.dcng.api.me.MessageExchangeManager;
import com.helger.dcng.core.CDcngVersion;

/**
 * Servlet for handling the initial calls without any path. This servlet shows
 * some basic information.
 *
 * @author Philip Helger
 */
@WebServlet ("")
public class DcngRootServlet extends HttpServlet
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngRootServlet.class);

  @Override
  protected void doGet (@Nonnull final HttpServletRequest req,
                        @Nonnull final HttpServletResponse resp) throws ServletException, IOException
  {
    final String sContextPath = req.getServletContext ().getContextPath ();
    final String sCSS = "* { font-family: sans-serif; }" +
                        " a:link, a:visited, a:hover, a:active { color: #2255ff; }" +
                        " code { font-family:monospace; color:#e83e8c; }";

    final StringBuilder aSB = new StringBuilder ();
    aSB.append ("<html><head><title>DE4A Connector NG</title><style>").append (sCSS).append ("</style></head><body>");
    aSB.append ("<h1>DE4A Connector NG</h1>");
    aSB.append ("<div>Version: ").append (CDcngVersion.BUILD_VERSION).append ("</div>");
    aSB.append ("<div>Build timestamp: ").append (CDcngVersion.BUILD_TIMESTAMP).append ("</div>");
    aSB.append ("<div>Current time: ").append (PDTFactory.getCurrentZonedDateTimeUTC ().toString ()).append ("</div>");
    aSB.append ("<div><a href='status'>Check /status</a></div>");
    aSB.append ("<div><a href='https://github.com/de4a-wp5/de4a-connector-ng' target='_blank'>Source code on GitHub</a></div>");

    {
      aSB.append ("<h2>Registered Message Exchange implementations</h2>");
      for (final Map.Entry <String, IMessageExchangeSPI> aEntry : CollectionHelper.getSortedByKey (MessageExchangeManager.getAll ())
                                                                                  .entrySet ())
      {
        aSB.append ("<div>ID <code>")
           .append (aEntry.getKey ())
           .append ("</code> mapped to ")
           .append (aEntry.getValue ())
           .append ("</div>");
      }
    }

    // if (GlobalDebug.isDebugMode ())
    {
      aSB.append ("<h2>Servlet information</h2>");
      for (final Map.Entry <String, ? extends ServletRegistration> aEntry : CollectionHelper.getSortedByKey (req.getServletContext ()
                                                                                                                .getServletRegistrations ())
                                                                                            .entrySet ())
      {
        aSB.append ("<div>Servlet <code>")
           .append (aEntry.getKey ())
           .append ("</code> mapped to ")
           .append (aEntry.getValue ().getMappings ())
           .append ("</div>");
      }
    }

    // APIs
    {
      aSB.append ("<h2>API information</h2>");

      aSB.append ("<h3>SMP</h3>");
      aSB.append ("<div>GET /api/smp/doctypes - <a href='" +
                  sContextPath +
                  "/api/smp/doctypes/iso6523-actorid-upis%3A%3A9915%3Ade4atest' target='_blank'>test me</a></div>");
      aSB.append ("<div>GET /api/smp/endpoints - <a href='" +
                  sContextPath +
                  "/api/smp/endpoints/iso6523-actorid-upis%3A%3A9915%3Ade4atest/urn:de4a-eu:CanonicalEvidenceType%3A%3ACompanyRegistration:1.0' target='_blank'>test me</a></div>");

      aSB.append ("<h3>Sending AS4</h3>");
      aSB.append ("<div>POST /api/it1/send</div>");
      aSB.append ("<div>POST /api/it1/lookup/send/</div>");

      aSB.append ("<div>POST /api/it2/send</div>");
      aSB.append ("<div>POST /api/it2/lookup/send/</div>");
    }

    aSB.append ("</body></html>");

    resp.addHeader (CHttpHeader.CONTENT_TYPE, CMimeType.TEXT_HTML.getAsString ());
    try
    {
      resp.getWriter ().write (aSB.toString ());
      resp.getWriter ().flush ();
    }
    catch (final IOException ex)
    {
      LOGGER.error ("Failed to write result", ex);
    }
  }
}
