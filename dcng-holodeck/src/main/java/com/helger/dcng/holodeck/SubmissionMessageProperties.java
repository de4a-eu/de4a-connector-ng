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
package com.helger.dcng.holodeck;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.peppolid.IParticipantIdentifier;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * This class represents a the message properties that will take place in the
 * outbound submission message
 *
 * @author myildiz at 15.02.2018.
 */
@SuppressFBWarnings ("NP_NONNULL_FIELD_NOT_INITIALIZED_IN_CONSTRUCTOR")
public class SubmissionMessageProperties
{

  /**
   * Ref to message id - referencing to the previous ebms message id if any
   */
  @Nullable
  public String refToMessageId;
  /**
   * Conversation ID
   */
  @Nullable
  public String conversationId;
  /**
   * EBMS message ID
   */
  @Nullable
  public String messageId;
  /**
   * TO party ID
   */
  @Nonnull
  public String toPartyId;
  /**
   * TO party Role
   */
  @Nonnull
  public String toPartyRole;

  /**
   * ToPartyIdType: The identification of the naming scheme of the used party
   * identifier
   */
  @Nullable
  public String toPartyIdType;

  /**
   * //CollaborationInfo/Service
   */
  @Nonnull
  public String service;

  /**
   * //CollaborationInfo/Service/@type
   */
  @Nullable
  public String serviceType;

  /**
   * //CollaborationInfo/Action
   */
  @Nonnull
  public String action;

  /**
   * ToPartyCertificate: the certificate of the destination gateway, to be used
   * for encryption
   */
  @Nonnull
  public String toPartyCertificate;

  /**
   * TargetURL: is the URL address of the destination AS4 gateway.
   */
  @Nonnull
  public String targetURL;

  /**
   * The participant identifier of the sender
   */
  public IParticipantIdentifier senderId;

  /**
   * The participant identifier of the receiver
   */
  public IParticipantIdentifier receiverId;
}
