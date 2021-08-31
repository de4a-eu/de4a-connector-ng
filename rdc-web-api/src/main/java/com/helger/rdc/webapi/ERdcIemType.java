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
package com.helger.rdc.webapi;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.phive.api.executorset.VESID;
import com.helger.rdc.api.rest.TCPayloadType;

/**
 * Contains the different top-level document types.
 *
 * @author Philip Helger
 */
public enum ERdcIemType
{
  REQUEST ("req", null, TCPayloadType.REQUEST),
  RESPONSE ("resp", null, TCPayloadType.RESPONSE);

  private final String m_sID;
  private final VESID m_aVESID;
  private final TCPayloadType m_ePayloadType;

  ERdcIemType (@Nonnull @Nonempty final String sID,
              @Nullable final VESID aVESID,
              @Nonnull final TCPayloadType ePayloadType)
  {
    m_sID = sID;
    m_aVESID = aVESID;
    m_ePayloadType = ePayloadType;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The validation key to be used for validating this top-level type.
   */
  @Nullable
  public VESID getVESID ()
  {
    return m_aVESID;
  }

  @Nonnull
  public TCPayloadType getPayloadType ()
  {
    return m_ePayloadType;
  }
}
