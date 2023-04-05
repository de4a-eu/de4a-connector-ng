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
