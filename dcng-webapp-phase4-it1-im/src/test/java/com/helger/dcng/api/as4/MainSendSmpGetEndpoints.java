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

public class MainSendSmpGetEndpoints
{
  private static final Logger LOGGER = LoggerFactory.getLogger (MainSendSmpGetEndpoints.class);

  public static void main (final String [] args) throws IOException
  {
    final String sReceiverID = CIdentifier.getURIPercentEncoded (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:ro000000006");
    final String sDocTypeID = CIdentifier.getURIPercentEncoded (DcngIdentifierFactory.DOCTYPE_SCHEME_CANONICAL_EVIDENCE, "CompanyRegistration");

    try (final HttpClientManager aHCM = new HttpClientManager ())
    {
      final HttpGet aGet = new HttpGet ("http://localhost:9092/api/smp/endpoints/" + sReceiverID + "/" + sDocTypeID);
      final IJson aJson = aHCM.execute (aGet, new ResponseHandlerJson ());
      LOGGER.info (new JsonWriter (new JsonWriterSettings ().setIndentEnabled (true)).writeAsString (aJson));
    }
  }
}
