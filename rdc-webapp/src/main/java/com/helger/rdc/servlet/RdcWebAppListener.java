/**
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
package com.helger.rdc.servlet;

import javax.annotation.Nonnull;
import javax.servlet.ServletContext;

import org.slf4j.LoggerFactory;

import com.helger.commons.io.ByteArrayWrapper;
import com.helger.photon.api.IAPIRegistry;
import com.helger.photon.audit.AuditHelper;
import com.helger.photon.audit.DoNothingAuditor;
import com.helger.photon.core.servlet.WebAppListener;
import com.helger.photon.security.login.LoggedInUserManager;
import com.helger.rdc.api.RdcConfig;
import com.helger.rdc.api.me.incoming.IMEIncomingHandler;
import com.helger.rdc.core.RdcInit;
import com.helger.rdc.webapi.RdcApiInit;

/**
 * Global startup etc. listener.
 *
 * @author Philip Helger
 */
public class RdcWebAppListener extends WebAppListener
{
  public RdcWebAppListener ()
  {
    setHandleStatisticsOnEnd (false);
  }

  @Override
  protected String getDataPath (@Nonnull final ServletContext aSC)
  {
    String ret = RdcConfig.WebApp.getDataPath ();
    if (ret == null)
    {
      // Fall back to servlet context path
      ret = super.getDataPath (aSC);
    }
    return ret;
  }

  @Override
  protected String getServletContextPath (final ServletContext aSC)
  {
    try
    {
      return super.getServletContextPath (aSC);
    }
    catch (final IllegalStateException ex)
    {
      // E.g. "Unpack WAR files" in Tomcat is disabled
      return getDataPath (aSC);
    }
  }

  @Override
  protected void afterContextInitialized (final ServletContext aSC)
  {
    // Use default handler
    final IMEIncomingHandler aDummyHdl = aMessage -> {
      if (aMessage.payloads ().size () >= 2)
      {
        final ByteArrayWrapper p = aMessage.payloads ().get (1).getData ();
        LoggerFactory.getLogger ("bla").info ("Canonical Evidence content:\n" + new String (p.bytes (), p.getOffset (), p.size ()));
      }
      else
        LoggerFactory.getLogger ("bla").info ("Invomcing messages seems to be ill-formatted");
    };
    RdcInit.initGlobally (aSC, aDummyHdl);

    // Don't write audit logs
    AuditHelper.setAuditor (new DoNothingAuditor (LoggedInUserManager.getInstance ()));
  }

  @Override
  protected void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    RdcApiInit.initAPI (aAPIRegistry);
  }

  @Override
  protected void beforeContextDestroyed (final ServletContext aSC)
  {
    RdcInit.shutdownGlobally (aSC);
  }
}
