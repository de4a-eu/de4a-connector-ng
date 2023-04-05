package com.helger.dcng.holodeck.notifications;

import javax.annotation.Nonnull;

/**
 * Implement this interface and register it to the MEMDelegate in order to
 * receive Notifications about the dispatch of the outbound message to the inner
 * corner of the receiving side
 *
 * @author yerlibilgin
 */
public interface IRelayResultHandler
{

  /**
   * Implement this method in order to receive Notifications about the dispatch
   * of the outbound message to the inner corner of the receiving side
   *
   * @param notification
   *        Notification relay result
   */
  void handleNotification (@Nonnull RelayResult notification);
}
