package com.helger.dcng.webapi.as4;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.annotation.Nonnull;
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
  private String m_sLookupEndpointURL;
  private ServiceMetadataType m_aLookupSM;
  private boolean m_bLookupSuccess = false;
  private boolean m_bSendingSuccess = false;
  private boolean m_bSuccess = false;
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

  public void setLookupEndpointURL (final String s)
  {
    m_sLookupEndpointURL = s;
  }

  public void setLookupServiceMetadata (final ServiceMetadataType aSM)
  {
    m_aLookupSM = aSM;
  }

  public void setLookupSuccess (final boolean b)
  {
    m_bLookupSuccess = b;
  }

  public void setSendingSuccess (final boolean b)
  {
    m_bSendingSuccess = b;
  }

  public boolean isSuccess ()
  {
    return m_bSuccess;
  }

  public void setSuccess (final boolean b)
  {
    m_bSuccess = b;
  }

  public void setException (final Exception ex)
  {
    m_aException = ex;
  }

  public void setInvocationDT (final ZonedDateTime aInvocationDT)
  {
    m_aInvocationDT = aInvocationDT;
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
    aJson.add (AbstractDcngApiInvoker.JSON_TAG_SUCCESS, m_bSuccess);

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
