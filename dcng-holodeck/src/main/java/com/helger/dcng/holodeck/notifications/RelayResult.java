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
