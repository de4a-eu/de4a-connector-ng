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
package com.helger.dcng.holodeck.notifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import com.helger.commons.CGlobal;
import com.helger.commons.ValueEnforcer;
import com.helger.commons.wrapper.Wrapper;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;

/**
 * @author yerlibilgin
 */
public class InternalNotificationHandler
{

  private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger (InternalNotificationHandler.class);

  private final Map <String, Wrapper <Notification>> messageQueue = new HashMap <> ();
  private final String targetTypeName;

  public InternalNotificationHandler (final Class <? extends Notification> targetType)
  {
    this.targetTypeName = targetType.getSimpleName ();

    // create a timer to periodically purge the expired notification and
    // submission
    // result messages

    final Timer timer = new Timer (targetTypeName + "-purgatory-timer");

    final long delay = 5 * CGlobal.MILLISECONDS_PER_MINUTE; // 5 minutes
    final long period = 5 * CGlobal.MILLISECONDS_PER_MINUTE; // 5 minutes

    timer.scheduleAtFixedRate (new TimerTask ()
    {
      @Override
      public void run ()
      {
        purgeExpiredNotifications ();
      }
    }, delay, period);
  }

  protected void handleNotification (final Notification notification)
  {
    Wrapper <Notification> carrier;

    LOG.info ("Received a notification " + notification.getMessageID () + "\n\t" + notification.getRefToMessageID ());
    // check the message quee and see if the new object is already there
    synchronized (messageQueue)
    {
      final String submitMessageID = notification.getRefToMessageID ();
      if (messageQueue.containsKey (submitMessageID))
      {
        carrier = messageQueue.get (submitMessageID);
      }
      else
      {
        carrier = new Wrapper <> ();
        messageQueue.put (submitMessageID, carrier);
      }
    }

    // now that we have a carrier, notify anyone who waits for it
    synchronized (carrier)
    {
      carrier.set (notification);
      carrier.notifyAll ();
    }
  }

  /**
   * Wait for a {@link Notification} for a message with the given
   * <code>submitMessageID</code> and for a maximum timeout of
   * <code>timeout</code>. Return the obtained notification
   *
   * @param submitMessageID
   *        the id of the submit message
   * @param timeout
   *        maximum amount to wait for the object. 0 means forever
   * @return the obtained {@link Notification}
   * @throws MEOutgoingException
   *         If waiting fails
   */
  public Notification obtainNotification (final String submitMessageID, final long timeout) throws MEOutgoingException
  {
    ValueEnforcer.isGE0 (timeout, "timeout");
    ValueEnforcer.notNull (submitMessageID, "MessageId");

    Wrapper <Notification> carrier = null;

    if (LOG.isDebugEnabled ())
      LOG.debug ("Wait for a " + targetTypeName + " with a messageID: " + submitMessageID);

    synchronized (messageQueue)
    {
      if (messageQueue.containsKey (submitMessageID))
      {
        if (LOG.isDebugEnabled ())
          LOG.debug ("we already have a " + targetTypeName + " message for " + submitMessageID);
        carrier = messageQueue.remove (submitMessageID);
      }
      else
      {
        // we don't have a carrier yet. Create one
        if (LOG.isDebugEnabled ())
          LOG.debug ("We don't have a " + targetTypeName + " waiter for " + submitMessageID + ". Create a waiter for it");

        carrier = new Wrapper <> ();
        messageQueue.put (submitMessageID, carrier);
      }
    }

    // we have a nunnull carrier here
    if (carrier.get () == null)
    {
      // we haven't received the actual object yet. So wait for it
      synchronized (carrier)
      {
        try
        {
          carrier.wait (timeout);
        }
        catch (final InterruptedException e)
        {
          if (LOG.isWarnEnabled ())
            LOG.warn ("Wait for message " + submitMessageID + " was interrupted.");
          Thread.currentThread ().interrupt ();
          throw new MEOutgoingException ("Wait for message " + submitMessageID + " was interrupted.", e);
        }
      }
    }

    if (carrier.get () == null)
    {
      throw new MEOutgoingException ("Couldn't obtain a " + targetTypeName + " with a messageID " + submitMessageID);
    }

    return carrier.get ();
  }

  /**
   * Check the notification and subm.result queue and purge the expired messages
   */
  private void purgeExpiredNotifications ()
  {
    final long currentTime = System.currentTimeMillis ();
    synchronized (messageQueue)
    {
      final List <String> trash = new ArrayList <> ();

      for (final Map.Entry <String, Wrapper <Notification>> entry : messageQueue.entrySet ())
      {
        final String messageID = entry.getKey ();
        final Wrapper <Notification> carrier = entry.getValue ();
        if (carrier != null && carrier.get () != null && carrier.get ().isExpired (currentTime))
        {
          trash.add (messageID);
        }
      }
      for (final String messageID : trash)
      {
        messageQueue.remove (messageID);
      }
    }
  }
}
