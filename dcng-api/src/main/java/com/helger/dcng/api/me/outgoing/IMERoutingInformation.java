package com.helger.dcng.api.me.outgoing;

import java.security.cert.X509Certificate;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Nonempty;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;

/**
 * Message Exchange Routing Information.
 *
 * @author Philip Helger
 */
public interface IMERoutingInformation
{
  /**
   * @return Sender participant ID. Never <code>null</code>.
   */
  @Nonnull
  IParticipantIdentifier getSenderID ();

  /**
   * @return Receiver participant ID. Never <code>null</code>.
   */
  @Nonnull
  IParticipantIdentifier getReceiverID ();

  /**
   * @return Document type ID. Never <code>null</code>.
   */
  @Nonnull
  IDocumentTypeIdentifier getDocumentTypeID ();

  /**
   * @return Process ID. Never <code>null</code>.
   */
  @Nonnull
  IProcessIdentifier getProcessID ();

  /**
   * @return The transport profile ID from the constructor. Neither
   *         <code>null</code> nor empty.
   */
  @Nonnull
  @Nonempty
  String getTransportProtocol ();

  /**
   * @return The endpoint URL from the SMP lookup. Neither <code>null</code> nor
   *         empty.
   */
  @Nonnull
  @Nonempty
  String getEndpointURL ();

  /**
   * @return The encoded certificate from the SMP look up. May not be
   *         <code>null</code>.
   */
  @Nonnull
  X509Certificate getCertificate ();
}
