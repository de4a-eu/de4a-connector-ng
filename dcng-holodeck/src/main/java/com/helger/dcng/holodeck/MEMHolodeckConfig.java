package com.helger.dcng.holodeck;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.config.IConfig;
import com.helger.dcng.api.DcngConfig;
import com.helger.dcng.api.me.EMEProtocol;

public final class MEMHolodeckConfig
{
  private MEMHolodeckConfig ()
  {}

  @Nonnull
  private static IConfig _getConfig ()
  {
    return DcngConfig.getConfig ();
  }

  /**
   * @return The MEM implementation ID or the default value. Never
   *         <code>null</code>.
   */
  @Nullable
  public static String getMEMImplementationID ()
  {
    return _getConfig ().getAsString ("toop.mem.implementation");
  }

  /**
   * Get the overall protocol to be used. Depending on that output different
   * other properties might be queried.
   *
   * @return The overall protocol to use. Never <code>null</code>.
   */
  @Nonnull
  public static EMEProtocol getMEMProtocol ()
  {
    final String sID = _getConfig ().getAsString ("toop.mem.protocol", EMEProtocol.DEFAULT.getID ());
    final EMEProtocol eProtocol = EMEProtocol.getFromIDOrNull (sID);
    if (eProtocol == null)
      throw new IllegalStateException ("Failed to resolve protocol with ID '" + sID + "'");
    return eProtocol;
  }

  // GW_URL
  @Nullable
  public static String getMEMAS4Endpoint ()
  {
    return _getConfig ().getAsString ("toop.mem.as4.endpoint");
  }

  @Nullable
  public static String getMEMAS4GwPartyID ()
  {
    return _getConfig ().getAsString ("toop.mem.as4.gw.partyid");
  }

  @Nullable
  public static String getMEMAS4TcPartyid ()
  {
    return _getConfig ().getAsString ("toop.mem.as4.tc.partyid");
  }

  /**
   * @return The <code>To/PartyId/@type</code> for receiving party id
   * @since 2.0.2
   */
  @Nullable
  public static String getToPartyIdType ()
  {
    return _getConfig ().getAsString ("toop.mem.as4.to-party-id-type");
  }

  public static long getGatewayNotificationWaitTimeout ()
  {
    return _getConfig ().getAsLong ("toop.mem.as4.notificationWaitTimeout", 20000);
  }

  /**
   * @return The DSC/DP URL where incoming AS4 messages are forwarded to. This
   *         is the value from the configuration file.
   */
  @Nullable
  public static String getMEMIncomingURL ()
  {
    return _getConfig ().getAsString ("toop.mem.incoming.url");
  }

  public static boolean isMEMOutgoingDumpEnabled ()
  {
    return _getConfig ().getAsBoolean ("toop.mem.outgoing.dump.enabled", false);
  }

  @Nullable
  public static String getMEMOutgoingDumpPath ()
  {
    return _getConfig ().getAsString ("toop.mem.outgoing.dump.path");
  }

  public static boolean isMEMIncomingDumpEnabled ()
  {
    return _getConfig ().getAsBoolean ("toop.mem.incoming.dump.enabled", false);
  }

  @Nullable
  public static String getMEMIncomingDumpPath ()
  {
    return _getConfig ().getAsString ("toop.mem.incoming.dump.path");
  }
}
