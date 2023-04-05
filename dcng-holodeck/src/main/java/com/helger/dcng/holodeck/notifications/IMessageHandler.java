package com.helger.dcng.holodeck.notifications;

import javax.annotation.Nonnull;

import com.helger.dcng.api.me.model.MEMessage;

/**
 * @author myildiz at 15.02.2018.
 */
public interface IMessageHandler
{
  /**
   * implement this method to receive messages when an inbound message arrives
   * to the AS4 endpoint
   * 
   * @param meMessage
   *        the object that contains the payloads and their metadata´
   * @throws Exception
   *         in case of error
   */
  void handleMessage (@Nonnull MEMessage meMessage) throws Exception;
}
