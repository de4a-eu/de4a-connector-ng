package com.helger.dcng.core.ial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.error.SingleError;
import com.helger.commons.error.list.ErrorList;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.ial.IIALClient;
import com.helger.dcng.core.http.DcngHttpClientSettings;
import com.helger.http.AcceptMimeTypeList;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerByteArray;
import com.helger.jaxb.validation.WrappedCollectingValidationEventHandler;

import eu.de4a.ial.api.IALMarshaller;
import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;

/**
 * Implementation of {@link IIALClient} that effectively performs an HTTP query
 * on the IAL service.
 *
 * @author Philip Helger
 * @since 0.2.4
 */
@Immutable
public final class DcngIALClientRemote implements IIALClient
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngIALClientRemote.class);
  private final String m_sBaseURL;

  public DcngIALClientRemote (@Nonnull @Nonempty final String sBaseURL)
  {
    ValueEnforcer.notEmpty (sBaseURL, "BaseURL");
    m_sBaseURL = sBaseURL;
  }

  @Nonnull
  @Nonempty
  public final String getBaseURL ()
  {
    return m_sBaseURL;
  }

  @Nullable
  public ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsOrderedSet <String> aCanonicalObjectTypeIDs,
                                                        @Nullable final String sATUCode,
                                                        @Nonnull final ErrorList aErrorList)
  {
    ValueEnforcer.notEmptyNoNullValue (aCanonicalObjectTypeIDs, "CanonicalObjectTypeIDs");
    ValueEnforcer.notNull (aErrorList, "ErrorList");

    final String sDestURL = FilenameHelper.getCleanConcatenatedUrlPath (m_sBaseURL,
                                                                        "/api/provision/" +
                                                                                    StringHelper.imploder ()
                                                                                                .source (aCanonicalObjectTypeIDs)
                                                                                                .separator (',')
                                                                                                .build () +
                                                                                    (StringHelper.hasText (sATUCode) ? "/" +
                                                                                                                       sATUCode
                                                                                                                     : ""));

    if (LOGGER.isInfoEnabled ())
      LOGGER.info ("Querying IAL via '" + sDestURL + "'");

    try (final HttpClientManager aHCM = HttpClientManager.create (new DcngHttpClientSettings ()))
    {
      final HttpGet aGet = new HttpGet (sDestURL);
      aGet.addHeader (CHttpHeader.ACCEPT,
                      new AcceptMimeTypeList ().addMimeType (CMimeType.APPLICATION_XML.getAsString (), 1)
                                               .getAsHttpHeaderValue ());
      final byte [] aResult = aHCM.execute (aGet, new ResponseHandlerByteArray ());

      if (LOGGER.isInfoEnabled ())
        LOGGER.info ("Queried IAL. Got " + ArrayHelper.getSize (aResult) + " bytes back");

      if (aResult == null)
        return null;

      final ResponseLookupRoutingInformationType ret = IALMarshaller.responseLookupRoutingInformationMarshaller ()
                                                                    .setValidationEventHandlerFactory (x -> new WrappedCollectingValidationEventHandler (aErrorList).andThen (x))
                                                                    .read (aResult);
      if (ret == null)
        LOGGER.error ("Failed to parse response of IAL query");
      return ret;
    }
    catch (final Exception ex)
    {
      LOGGER.error ("Failed to query IAL at '" + sDestURL + "'", ex);
      aErrorList.add (SingleError.builderError ()
                                 .errorText ("Failed to query IAL at '" + sDestURL + "'")
                                 .linkedException (ex)
                                 .build ());
    }
    return null;
  }

  @Nonnull
  public static DcngIALClientRemote createDefaultInstance ()
  {
    // Use the configured value as the base URL
    return new DcngIALClientRemote (DcngConfig.IAL.getIALUrl ());
  }
}
