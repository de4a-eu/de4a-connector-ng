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
package com.helger.dcng.api.me;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.id.IHasID;
import com.helger.commons.lang.EnumHelper;
import com.helger.peppol.smp.ESMPTransportProfile;

/**
 * Contains all protocols supported for message exchange
 *
 * @author Philip Helger
 */
public enum EMEProtocol implements IHasID <String>
{
  /**
   * CEF AS4.
   */
  AS4 ("as4", ESMPTransportProfile.TRANSPORT_PROFILE_BDXR_AS4.getID ());

  public static final EMEProtocol DEFAULT = AS4;

  private final String m_sID;
  private final String m_sTransportProfileID;

  EMEProtocol (@Nonnull @Nonempty final String sID, @Nonnull @Nonempty final String sTransportProfileID)
  {
    m_sID = sID;
    m_sTransportProfileID = sTransportProfileID;
  }

  @Nonnull
  @Nonempty
  public String getID ()
  {
    return m_sID;
  }

  /**
   * @return The transport profile ID for the SMP to be used for this MP
   *         protocol. Neither <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  public String getTransportProfileID ()
  {
    return m_sTransportProfileID;
  }

  @Nullable
  public static EMEProtocol getFromIDOrNull (@Nullable final String sID)
  {
    return EnumHelper.getFromIDOrNull (EMEProtocol.class, sID);
  }
}
