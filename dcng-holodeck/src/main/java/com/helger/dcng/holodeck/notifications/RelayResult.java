package com.helger.dcng.holodeck.notifications;

/**
 * A java representation of a notification C2 --- C3 message relay. See TOOP AS4
 * GW backend interface specification
 *
 * @author yerlibilgin
 */
public class RelayResult extends Notification
{
  private String m_sShortDescription;
  private String m_sSeverity;

  public String getShortDescription ()
  {
    return m_sShortDescription;
  }

  public void setShortDescription (final String shortDescription)
  {
    this.m_sShortDescription = shortDescription;
  }

  public void setSeverity (final String severity)
  {
    this.m_sSeverity = severity;
  }

  public String getSeverity ()
  {
    return m_sSeverity;
  }
}
