package com.helger.dcng.phase4.servlet;

import javax.servlet.annotation.WebServlet;

import com.helger.commons.http.EHttpMethod;
import com.helger.dcng.phase4.Phase4Config;
import com.helger.phase4.attachment.IAS4IncomingAttachmentFactory;
import com.helger.phase4.model.pmode.resolve.DefaultPModeResolver;
import com.helger.phase4.model.pmode.resolve.IPModeResolver;
import com.helger.phase4.servlet.AS4XServletHandler;
import com.helger.xservlet.AbstractXServlet;

/**
 * Local AS4 servlet. This endpoint (full deployed URL) must be registered in
 * SMP endpoints for receiving messages.
 *
 * @author Philip Helger
 */
@WebServlet ("/phase4")
public class AS4ReceiveServlet extends AbstractXServlet
{
  public AS4ReceiveServlet ()
  {
    // Multipart is handled specifically inside
    settings ().setMultipartEnabled (false);

    // The servlet handler takes all SPI implementations of
    // IAS4ServletMessageProcessorSPI and invokes them.
    // -> see AS4MessageProcessorSPI
    final IPModeResolver aPMR = DefaultPModeResolver.DEFAULT_PMODE_RESOLVER;
    final IAS4IncomingAttachmentFactory aIAF = IAS4IncomingAttachmentFactory.DEFAULT_INSTANCE;
    handlerRegistry ().registerHandler (EHttpMethod.POST, new AS4XServletHandler (Phase4Config::getCryptoFactory, aPMR, aIAF));
  }
}
