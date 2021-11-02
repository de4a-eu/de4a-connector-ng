/*
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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
package com.helger.dcng.api.me.model;

import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.Nonempty;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.annotation.ReturnsMutableObject;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;

/**
 * List of {@link MEPayload} objects. The first payload MUST be the main user
 * message.
 *
 * @author Philip Helger
 */
public class MEMessage
{
  private final IParticipantIdentifier m_aSenderID;
  private final IParticipantIdentifier m_aReceiverID;
  private final IDocumentTypeIdentifier m_aDocTypeID;
  private final IProcessIdentifier m_aProcessID;
  private final ICommonsList <MEPayload> m_aPayloads = new CommonsArrayList <> ();

  /**
   * Instantiates a new Me message.
   *
   * @param aSenderID
   *        Sender ID
   * @param aReceiverID
   *        Receiver ID
   * @param aDocTypeID
   *        Document type ID
   * @param aProcessID
   *        Process ID
   * @param aPayloads
   *        the payloads
   */
  protected MEMessage (@Nullable final IParticipantIdentifier aSenderID,
                       @Nullable final IParticipantIdentifier aReceiverID,
                       @Nullable final IDocumentTypeIdentifier aDocTypeID,
                       @Nullable final IProcessIdentifier aProcessID,
                       @Nonnull @Nonempty final ICommonsList <MEPayload> aPayloads)
  {
    ValueEnforcer.notEmptyNoNullValue (aPayloads, "Payloads");
    m_aSenderID = aSenderID;
    m_aReceiverID = aReceiverID;
    m_aDocTypeID = aDocTypeID;
    m_aProcessID = aProcessID;
    m_aPayloads.addAll (aPayloads);
  }

  /**
   * @return the sender participant id. Maybe <code>null</code>.
   */
  @Nullable
  public IParticipantIdentifier getSenderID ()
  {
    return m_aSenderID;
  }

  /**
   * @return the receiver participant id. Maybe <code>null</code>.
   */
  @Nullable
  public IParticipantIdentifier getReceiverID ()
  {
    return m_aReceiverID;
  }

  /**
   * @return the document type id. Maybe <code>null</code>.
   */
  @Nullable
  public IDocumentTypeIdentifier getDocumentTypeID ()
  {
    return m_aDocTypeID;
  }

  /**
   * @return the process id. Maybe <code>null</code>.
   */
  @Nullable
  public IProcessIdentifier getProcessID ()
  {
    return m_aProcessID;
  }

  /**
   * @return A non-<code>null</code>, non-empty list of payloads. The result
   *         object is mutable and can change the content of this object.
   */
  @Nonnull
  @Nonempty
  @ReturnsMutableObject
  public ICommonsList <MEPayload> payloads ()
  {
    return m_aPayloads;
  }

  /**
   * @return A non-<code>null</code>, non-empty list of payloads. The result
   *         object is a cloned list.
   */
  @Nonnull
  @Nonempty
  @ReturnsMutableCopy
  public ICommonsList <MEPayload> getAllPayloads ()
  {
    return m_aPayloads.getClone ();
  }

  /**
   * Builder builder.
   *
   * @return the builder
   */
  @Nonnull
  public static Builder builder ()
  {
    return new Builder ();
  }

  /**
   * Builder class for {@link MEMessage}.
   *
   * @author Philip Helger
   */
  public static class Builder
  {
    private IParticipantIdentifier m_aSenderID;
    private IParticipantIdentifier m_aReceiverID;
    private IDocumentTypeIdentifier m_aDocTypeID;
    private IProcessIdentifier m_aProcessID;
    private final ICommonsList <MEPayload> m_aPayloads = new CommonsArrayList <> ();

    /**
     * Instantiates a new Builder.
     */
    protected Builder ()
    {}

    /**
     * Sets sender id.
     *
     * @param a
     *        the sender id
     * @return the builder
     */
    @Nonnull
    public Builder senderID (@Nullable final IParticipantIdentifier a)
    {
      m_aSenderID = a;
      return this;
    }

    /**
     * Sets receiver id.
     *
     * @param a
     *        the receiver id
     * @return the builder
     */
    @Nonnull
    public Builder receiverID (@Nullable final IParticipantIdentifier a)
    {
      m_aReceiverID = a;
      return this;
    }

    /**
     * Sets document type id.
     *
     * @param a
     *        the document type id
     * @return the builder
     */
    @Nonnull
    public Builder docTypeID (@Nullable final IDocumentTypeIdentifier a)
    {
      m_aDocTypeID = a;
      return this;
    }

    /**
     * Sets process id.
     *
     * @param a
     *        the process id
     * @return the builder
     */
    @Nonnull
    public Builder processID (@Nullable final IProcessIdentifier a)
    {
      m_aProcessID = a;
      return this;
    }

    /**
     * Add payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder addPayload (@Nullable final Consumer <? super MEPayload.Builder> a)
    {
      if (a != null)
      {
        final MEPayload.Builder aBuilder = MEPayload.builder ();
        a.accept (aBuilder);
        addPayload (aBuilder);
      }
      return this;
    }

    /**
     * Add payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder addPayload (@Nullable final MEPayload.Builder a)
    {
      return addPayload (a == null ? null : a.build ());
    }

    /**
     * Add payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder addPayload (@Nullable final MEPayload a)
    {
      if (a != null)
        m_aPayloads.add (a);
      return this;
    }

    /**
     * Payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder payload (@Nullable final Consumer <? super MEPayload.Builder> a)
    {
      if (a != null)
      {
        final MEPayload.Builder aBuilder = MEPayload.builder ();
        a.accept (aBuilder);
        payload (aBuilder);
      }
      return this;
    }

    /**
     * Payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder payload (@Nullable final MEPayload.Builder a)
    {
      return payload (a == null ? null : a.build ());
    }

    /**
     * Payload builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder payload (@Nullable final MEPayload a)
    {
      if (a != null)
        m_aPayloads.set (a);
      else
        m_aPayloads.clear ();
      return this;
    }

    /**
     * Payloads builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder payloads (@Nullable final MEPayload... a)
    {
      m_aPayloads.setAll (a);
      return this;
    }

    /**
     * Payloads builder.
     *
     * @param a
     *        the a
     * @return the builder
     */
    @Nonnull
    public Builder payloads (@Nullable final Iterable <MEPayload> a)
    {
      m_aPayloads.setAll (a);
      return this;
    }

    /**
     * Check consistency.
     */
    public void checkConsistency ()
    {
      if (m_aPayloads.isEmpty ())
        throw new IllegalStateException ("At least one payload MUST be present");
    }

    /**
     * Build me message.
     *
     * @return the me message
     */
    @Nonnull
    public MEMessage build ()
    {
      checkConsistency ();
      return new MEMessage (m_aSenderID, m_aReceiverID, m_aDocTypeID, m_aProcessID, m_aPayloads);
    }
  }
}
