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
