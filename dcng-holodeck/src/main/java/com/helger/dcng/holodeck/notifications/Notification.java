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
package com.helger.dcng.holodeck.notifications;

import java.io.Serializable;

import com.helger.commons.CGlobal;
import com.helger.dcng.holodeck.EResultType;

/**
 * @author yerlibilgin
 */
public class Notification implements Serializable
{

  private static final long EXPIRATION_PERIOD = 5 * CGlobal.MILLISECONDS_PER_MINUTE;
  /**
   * The message id of the SUBMIT message (C1 --&gt; C2)
   */
  private String m_sMessageID;
  /**
   * The message id of the outbound message (C2 --&gt; C3)
   */
  private String m_sRefToMessageID;
  /**
   * The type of this notification
   */
  private EResultType m_eResult;
  /**
   * The context specific error code (or null in case of success)
   */
  private String m_sErrorCode;
  /**
   * Long description if any
   */
  private String m_sDescription;

  /**
   * The local milliseconds time when this object was created
   */
  private final long m_nCreationTime;

  Notification ()
  {
    m_nCreationTime = System.currentTimeMillis ();
  }

  public String getRefToMessageID ()
  {
    return m_sRefToMessageID;
  }

  public void setRefToMessageID (final String refToMessageID)
  {
    m_sRefToMessageID = refToMessageID;
  }

  public EResultType getResult ()
  {
    return m_eResult;
  }

  public void setResult (final EResultType result)
  {
    m_eResult = result;
  }

  public String getErrorCode ()
  {
    return m_sErrorCode;
  }

  public void setErrorCode (final String errorCode)
  {
    m_sErrorCode = errorCode;
  }

  public String getDescription ()
  {
    return m_sDescription;
  }

  public void setDescription (final String description)
  {
    m_sDescription = description;
  }

  @Override
  public String toString ()
  {
    return "Notification for " + m_sRefToMessageID;
  }

  public String getMessageID ()
  {
    return m_sMessageID;
  }

  public void setMessageID (final String messageID)
  {
    m_sMessageID = messageID;
  }

  public boolean isExpired (final long currentTime)
  {
    return (currentTime - m_nCreationTime) > EXPIRATION_PERIOD;
  }
}
