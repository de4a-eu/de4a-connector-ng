/*
 * Copyright (C) 2023, Partners of the EU funded DE4A project consortium
 *   (https://www.de4a.eu/consortium), under Grant Agreement No.870635
 * Author: Austrian Federal Computing Center (BRZ)
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
package com.helger.dcng.webapi.as4;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;

import com.helger.commons.string.StringHelper;
import com.helger.dcng.webapi.helper.AbstractDcngApiInvoker;
import com.helger.json.IJsonObject;
import com.helger.json.JsonObject;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.phive.json.PhiveJsonHelper;
import com.helger.smpclient.json.SMPJsonResponse;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

@NotThreadSafe
public class LookupAndSendingResult
{
  public static final String JSON_TAG_SENDER_ID = "senderID";
  public static final String JSON_TAG_RECEIVER_ID = "receiverID";
  public static final String JSON_TAG_RESPONSE = "response";
  public static final String JSON_TAG_RESULT_LOOKUP = "lookupResults";
  public static final String JSON_TAG_RESULT_SEND = "sendingResults";
  public static final String JSON_TAG_EXCEPTION = "exception";
  public static final String JSON_TAG_INVOCATION_DATE_TIME = "invocationDateTime";
  public static final String JSON_TAG_INVOCATION_DURATION_MILLIS = "invocationDurationMillis";

  private final IParticipantIdentifier m_aSenderID;
  private final IParticipantIdentifier m_aReceiverID;
  private final IDocumentTypeIdentifier m_aDocumentTypeID;
  private final IProcessIdentifier m_aProcessID;
  private final String m_sTransportProfile;

  private ServiceMetadataType m_aLookupSM;
  private String m_sLookupEndpointURL;
  private boolean m_bLookupSuccess = false;

  private boolean m_bSendingSuccess = false;

  private boolean m_bOverallSuccess = false;
  private Exception m_aException;
  private ZonedDateTime m_aInvocationDT;
  private long m_nDurationMS = -1;

  public LookupAndSendingResult (@Nonnull final IParticipantIdentifier aSenderID,
                                 @Nonnull final IParticipantIdentifier aReceiverID,
                                 @Nonnull final IDocumentTypeIdentifier aDocumentTypeID,
                                 @Nonnull final IProcessIdentifier aProcessID,
                                 @Nonnull final String sTransportProfile)
  {
    m_aSenderID = aSenderID;
    m_aReceiverID = aReceiverID;
    m_aDocumentTypeID = aDocumentTypeID;
    m_aProcessID = aProcessID;
    m_sTransportProfile = sTransportProfile;
  }

  @Nullable
  public ServiceMetadataType getLookupServiceMetadata ()
  {
    return m_aLookupSM;
  }

  public boolean hasLookupServiceMetadata ()
  {
    return m_aLookupSM != null;
  }

  public void setLookupServiceMetadata (@Nullable final ServiceMetadataType aSM)
  {
    m_aLookupSM = aSM;
  }

  @Nullable
  public String getLookupEndpointURL ()
  {
    return m_sLookupEndpointURL;
  }

  public void setLookupEndpointURL (@Nullable final String s)
  {
    m_sLookupEndpointURL = s;
  }

  public boolean isLookupSuccess ()
  {
    return m_bLookupSuccess;
  }

  public void setLookupSuccess (final boolean b)
  {
    m_bLookupSuccess = b;
  }

  public boolean isSendingSuccess ()
  {
    return m_bSendingSuccess;
  }

  public void setSendingSuccess (final boolean b)
  {
    m_bSendingSuccess = b;
  }

  public boolean isOverallSuccess ()
  {
    return m_bOverallSuccess;
  }

  public void setOverallSuccess (final boolean b)
  {
    m_bOverallSuccess = b;
  }

  @Nullable
  public Exception getException ()
  {
    return m_aException;
  }

  public boolean hasException ()
  {
    return m_aException != null;
  }

  public void setException (@Nullable final Exception ex)
  {
    m_aException = ex;
  }

  @Nullable
  public ZonedDateTime getInvocationDT ()
  {
    return m_aInvocationDT;
  }

  public void setInvocationDT (@Nullable final ZonedDateTime aInvocationDT)
  {
    m_aInvocationDT = aInvocationDT;
  }

  public long getDurationMS ()
  {
    return m_nDurationMS;
  }

  public void setDurationMS (final long n)
  {
    m_nDurationMS = n;
  }

  @Nonnull
  public IJsonObject getAsJson ()
  {
    final IJsonObject aJson = new JsonObject ();
    aJson.add (JSON_TAG_SENDER_ID, m_aSenderID.getURIEncoded ());
    aJson.add (JSON_TAG_RECEIVER_ID, m_aReceiverID.getURIEncoded ());
    aJson.add (SMPJsonResponse.JSON_DOCUMENT_TYPE_ID, m_aDocumentTypeID.getURIEncoded ());
    aJson.add (SMPJsonResponse.JSON_PROCESS_ID, m_aProcessID.getURIEncoded ());
    aJson.add (SMPJsonResponse.JSON_TRANSPORT_PROFILE, m_sTransportProfile);
    aJson.add (AbstractDcngApiInvoker.JSON_TAG_SUCCESS, m_bOverallSuccess);

    // Remember lookup stuff
    {
      final IJsonObject aObj = new JsonObject ();
      aObj.add (AbstractDcngApiInvoker.JSON_TAG_SUCCESS, m_bLookupSuccess);
      if (m_aLookupSM != null)
        aObj.addJson (JSON_TAG_RESPONSE, SMPJsonResponse.convert (m_aReceiverID, m_aDocumentTypeID, m_aLookupSM));
      if (StringHelper.hasText (m_sLookupEndpointURL))
        aObj.add (SMPJsonResponse.JSON_ENDPOINT_REFERENCE, m_sLookupEndpointURL);
      aJson.addJson (JSON_TAG_RESULT_LOOKUP, aObj);
    }

    // Remember sending stuff
    {
      final IJsonObject aObj = new JsonObject ();
      aObj.add (AbstractDcngApiInvoker.JSON_TAG_SUCCESS, m_bSendingSuccess);
      aJson.addJson (JSON_TAG_RESULT_SEND, aObj);
    }

    if (m_aException != null)
      aJson.addJson (JSON_TAG_EXCEPTION, PhiveJsonHelper.getJsonStackTrace (m_aException));

    if (m_aInvocationDT != null)
      aJson.add (JSON_TAG_INVOCATION_DATE_TIME, DateTimeFormatter.ISO_ZONED_DATE_TIME.format (m_aInvocationDT));
    if (m_nDurationMS >= 0)
      aJson.add (JSON_TAG_INVOCATION_DURATION_MILLIS, m_nDurationMS);

    return aJson;
  }
}
