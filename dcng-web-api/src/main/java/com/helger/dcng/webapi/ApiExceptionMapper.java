package com.helger.dcng.webapi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletResponse;

import org.apache.hc.client5.http.HttpResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.debug.GlobalDebug;
import com.helger.commons.state.EHandled;
import com.helger.commons.string.StringHelper;
import com.helger.photon.api.AbstractAPIExceptionMapper;
import com.helger.photon.api.InvokableAPIDescriptor;
import com.helger.servlet.response.UnifiedResponse;
import com.helger.web.scope.IRequestWebScopeWithoutResponse;

/**
 * Special API exception mapper for the REST API.
 *
 * @author Philip Helger
 */
public class ApiExceptionMapper extends AbstractAPIExceptionMapper
{
  private static final Logger LOGGER = LoggerFactory.getLogger (ApiExceptionMapper.class);

  private static void _logRestException (@Nonnull final String sMsg, @Nonnull final Throwable t)
  {
    LOGGER.error (sMsg, t);
  }

  private static void _setSimpleTextResponse (@Nonnull final UnifiedResponse aUnifiedResponse,
                                              final int nStatusCode,
                                              @Nullable final String sContent)
  {
    if (GlobalDebug.isDebugMode ())
    {
      // With payload
      setSimpleTextResponse (aUnifiedResponse, nStatusCode, sContent);
      if (StringHelper.hasText (sContent))
        aUnifiedResponse.disableCaching ();
    }
    else
    {
      // No payload
      aUnifiedResponse.setStatus (nStatusCode);
    }
  }

  @Nonnull
  public EHandled applyExceptionOnResponse (@Nonnull final InvokableAPIDescriptor aInvokableDescriptor,
                                            @Nonnull final IRequestWebScopeWithoutResponse aRequestScope,
                                            @Nonnull final UnifiedResponse aUnifiedResponse,
                                            @Nonnull final Throwable aThrowable)
  {
    // From specific to general
    if (aThrowable instanceof HttpResponseException)
    {
      // HTTP from caught exception
      _logRestException ("HttpResponse exception", aThrowable);
      final HttpResponseException aEx = (HttpResponseException) aThrowable;
      _setSimpleTextResponse (aUnifiedResponse, aEx.getStatusCode (), aEx.getReasonPhrase ());
      return EHandled.HANDLED;
    }
    if (aThrowable instanceof ApiParamException)
    {
      // HTTP 400
      _logRestException ("Parameter exception", aThrowable);
      _setSimpleTextResponse (aUnifiedResponse,
                              HttpServletResponse.SC_BAD_REQUEST,
                              GlobalDebug.isDebugMode () ? getResponseEntityWithStackTrace (aThrowable)
                                                         : getResponseEntityWithoutStackTrace (aThrowable));
      return EHandled.HANDLED;
    }
    if (aThrowable instanceof RuntimeException)
    {
      // HTTP 500
      _logRestException ("Runtime exception - " + aThrowable.getClass ().getName (), aThrowable);
      _setSimpleTextResponse (aUnifiedResponse,
                              HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                              GlobalDebug.isDebugMode () ? getResponseEntityWithStackTrace (aThrowable)
                                                         : getResponseEntityWithoutStackTrace (aThrowable));
      return EHandled.HANDLED;
    }

    if (LOGGER.isDebugEnabled ())
      LOGGER.debug ("Received unhandled Exception of type " +
                    (aThrowable != null ? aThrowable.getClass ().getName () : "null"));

    // We don't know that exception
    return EHandled.UNHANDLED;
  }
}
