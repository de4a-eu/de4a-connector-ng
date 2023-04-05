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
package com.helger.dcng.api.error;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.lang.EnumHelper;

/**
 * Source: ErrorCode-CodeList.gc<br>
 * Content created by MainCreateJavaCode_ErrorCode_GC
 *
 * @author Philip Helger
 */
public enum EDcngErrorCode implements IDcngErrorCode
{
  /** Uncategorized error */
  GEN ("GEN"),
  /** The payload provided from DC/DP to the Connector is not valid */
  IF_001 ("IF-001"),
  /** Message Validation Failed */
  IF_002 ("IF-002"),
  /** The Directory is not reachable */
  DD_001 ("DD-001"),
  /** An SMP could not be queried */
  DD_002 ("DD-002"),
  /** Error validating a signature from SMP */
  DD_003 ("DD-003"),
  /**
   * The Dynamic Discovery Service was not able to find any Participant
   * Identifiers
   */
  DD_004 ("DD-004"),
  /**
   * The Connector was not able to communicate with the Local AS4 gateway
   */
  ME_001 ("ME-001"),
  /** The AS4 Gateway was not able to send the message */
  ME_002 ("ME-002"),
  /** The AS4 gateway could not deliver the message to the addressed gateway */
  ME_003 ("ME-003"),
  /** The AS4 gateway has not received a receipt */
  ME_004 ("ME-004");

  private final String m_sID;

  EDcngErrorCode (@Nonnull @Nonempty final String sID)
  {
    m_sID = sID;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  @Nullable
  public static EDcngErrorCode getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EDcngErrorCode.class, sID);
  }
}
