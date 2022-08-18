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
package com.helger.dcng.api.as4;

import java.io.IOException;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.dcng.api.DcngIdentifierFactory;
import com.helger.httpclient.HttpClientManager;
import com.helger.httpclient.response.ResponseHandlerJson;
import com.helger.json.IJson;
import com.helger.json.serialize.JsonWriter;
import com.helger.json.serialize.JsonWriterSettings;
import com.helger.peppolid.CIdentifier;

public class MainSendSmpGetDocTypes
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendSmpGetDocTypes.class);

  public static void main (final String [] args) throws IOException
  {
    final String sReceiverID = CIdentifier.getURIPercentEncoded (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:ro000000006");

    try (final HttpClientManager aHCM = new HttpClientManager ())
    {
      final HttpGet aGet = new HttpGet ("http://localhost:9092/api/smp/doctypes/" + sReceiverID);
      final IJson aJson = aHCM.execute (aGet, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
