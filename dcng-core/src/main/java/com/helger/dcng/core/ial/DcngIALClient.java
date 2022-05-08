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
package com.helger.dcng.core.ial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import org.apache.http.client.methods.HttpGet;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.ArrayHelper;
import com.helger.commons.collection.impl.ICommonsSortedSet;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.commons.http.CHttpHeader;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.mime.CMimeType;
import com.helger.commons.string.StringHelper;
import com.helger.dcng.core.http.DcngHttpClientSettings;
import com.helger.http.AcceptMimeTypeList;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerByteArray;

import eu.de4a.ial.api.IALMarshaller;
import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

@NotThreadSafe
public final class DcngIALClient
{
  private DcngIALClient ()
  {}

  @Nullable
  public static ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsSortedSet <String> aCanonicalObjectTypeIDs,
                                                               @Nullable final String sATUCode)
  {
    ValueEnforcer.notEmptyNoNullValue (aCanonicalObjectTypeIDs, "CanonicalObjectTypeIDs");

    final String sDestURL = FilenameHelper.getCleanConcatenatedUrlPath ("",
                                                                        "/api/provision/" +
                                                                            StringHelper.imploder ()
                                                                                        .source (aCanonicalObjectTypeIDs)
                                                                                        .separator (',')
                                                                                        .build () +
                                                                            (StringHelper.hasText (sATUCode) ? "/" +
                                                                                                               sATUCode
                                                                                                             : ""));

    // Main sending, using DCNG http settings
    try (final HttpClientManager aHCM = HttpClientManager.create (new DcngHttpClientSettings ()))
    {
      final HttpGet aGet = new HttpGet (sDestURL);
      aGet.addHeader (CHttpHeader.ACCEPT,
                      new AcceptMimeTypeList ().addMimeType (CMimeType.APPLICATION_XML.getAsString (), 1)
                                               .getAsHttpHeaderValue ());
      final byte [] aResult = aHCM.execute (aGet, new ResponseHandlerByteArray ());

      DE4AKafkaClient.send (EErrorLevel.INFO,
                            () -> "Queried IAL. Got " + ArrayHelper.getSize (aResult) + " bytes back");

      final ResponseLookupRoutingInformationType ret = IALMarshaller.idkResponseLookupRoutingInformationMarshaller ()
                                                                    .read (aResult);
      if (ret == null)
        DE4AKafkaClient.send (EErrorLevel.WARN, () -> "Failed to parse response of IAL query");

      return ret;
    }
    catch (final Exception ex)
    {
      DE4AKafkaClient.send (EErrorLevel.ERROR, () -> "Failed to query IAL at '" + sDestURL + "'", ex);
    }
    return null;
  }
}
