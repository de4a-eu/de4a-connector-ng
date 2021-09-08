/**
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
package com.helger.rdc.core.api;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.rdc.api.error.LoggingRdcErrorHandler;
import com.helger.rdc.api.me.IMessageExchangeSPI;
import com.helger.rdc.api.me.MessageExchangeManager;
import com.helger.rdc.api.me.model.MEMessage;
import com.helger.rdc.api.me.outgoing.IMERoutingInformation;
import com.helger.rdc.api.me.outgoing.MEOutgoingException;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * A utility class that provides abstractions for all major tasks to be invoked
 * by the TC and is also used from the REST API components.
 *
 * @author Philip Helger
 */
@Immutable
public final class RdcApiHelper
{
  public static final Locale DEFAULT_LOCALE = Locale.US;

  private RdcApiHelper ()
  {}

  /**
   * @param aParticipantID
   *        Participant ID to query. May not be <code>null</code>.
   * @return A non-<code>null</code> sorted map of all hrefs. The key MUST be
   *         URL decoded whereas the value is the "original href" as found in
   *         the response.
   */
  @Nonnull
  public static ICommonsSortedMap <String, String> querySMPServiceGroups (@Nonnull final IParticipantIdentifier aParticipantID)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "Querying SMP service groups for " + aParticipantID.getURIEncoded ());

    return RdcApiConfig.getDDServiceGroupHrefProvider ().getAllServiceGroupHrefs (aParticipantID, LoggingRdcErrorHandler.INSTANCE);
  }

  /**
   * Find the service metadata. This returns all endpoints for the combination.
   *
   * @param aParticipantID
   *        Participant ID to query. May not be <code>null</code>.
   * @param aDocTypeID
   *        Document type ID. May not be <code>null</code>. Required only if the
   *        usage of DNS is disabled.
   * @param aProcessID
   *        Process ID. May not be <code>null</code>. Required only if the usage
   *        of DNS is disabled.
   * @param sTransportProfile
   *        Transport profile ID. May not be <code>null</code>. Required only if
   *        the usage of DNS is disabled.
   * @return <code>null</code> if not found.
   */
  @Nullable
  public static ServiceMetadataType querySMPServiceMetadata (@Nonnull final IParticipantIdentifier aParticipantID,
                                                             @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                                             @Nonnull final IProcessIdentifier aProcessID,
                                                             @Nonnull final String sTransportProfile)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "Querying SMP service metadata for " +
                                aParticipantID.getURIEncoded () +
                                " [and " +
                                aDocTypeID.getURIEncoded () +
                                " and " +
                                aProcessID.getURIEncoded () +
                                " and " +
                                sTransportProfile +
                                "]");

    final ServiceMetadataType ret = RdcApiConfig.getDDServiceMetadataProvider ()
                                                .getServiceMetadata (aParticipantID, aDocTypeID, aProcessID, sTransportProfile);

    if (ret == null)
      DE4AKafkaClient.send (EErrorLevel.WARN, "Found no matching SMP service metadata");
    else
      DE4AKafkaClient.send (EErrorLevel.INFO, "Found matching SMP service metadata");
    return ret;
  }

  /**
   * Find the dynamic discovery endpoint from the respective parameters. This
   * calls
   * {@link #querySMPServiceMetadata(IParticipantIdentifier, IDocumentTypeIdentifier, IProcessIdentifier, String)}
   * and filters out the matching process ID and transport profile ID.
   *
   * @param aParticipantID
   *        Participant ID to query. May not be <code>null</code>.
   * @param aDocTypeID
   *        Document type ID. May not be <code>null</code>.
   * @param aProcessID
   *        Process ID. May not be <code>null</code>.
   * @param sTransportProfile
   *        Transport profile to be used. May not be <code>null</code>.
   * @return <code>null</code> if no such endpoint was found
   */
  @Nullable
  public static EndpointType querySMPEndpoint (@Nonnull final IParticipantIdentifier aParticipantID,
                                               @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                               @Nonnull final IProcessIdentifier aProcessID,
                                               @Nonnull final String sTransportProfile)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "Querying SMP endpoint for " +
                                aParticipantID.getURIEncoded () +
                                " and " +
                                aDocTypeID.getURIEncoded () +
                                " and " +
                                aProcessID.getURIEncoded () +
                                " and " +
                                sTransportProfile);

    return RdcApiConfig.getDDServiceMetadataProvider ().getEndpoint (aParticipantID, aDocTypeID, aProcessID, sTransportProfile);
  }

  /**
   * Send an AS4 message using the configured Message Exchange Module (MEM).
   *
   * @param aRoutingInfo
   *        Routing information. May not be <code>null</code>.
   * @param aMessage
   *        The message to be exchanged. May not be <code>null</code>.
   * @throws MEOutgoingException
   *         In case of error.
   */
  public static void sendAS4Message (@Nonnull final IMERoutingInformation aRoutingInfo,
                                     @Nonnull final MEMessage aMessage) throws MEOutgoingException
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "Sending from '" +
                                aRoutingInfo.getSenderID ().getURIEncoded () +
                                "' to '" +
                                aRoutingInfo.getReceiverID ().getURIEncoded () +
                                "' using doctype '" +
                                aRoutingInfo.getDocumentTypeID ().getURIEncoded () +
                                "' and process '" +
                                aRoutingInfo.getProcessID ().getURIEncoded () +
                                "' to URL " +
                                aRoutingInfo.getEndpointURL ());

    final IMessageExchangeSPI aMEM = MessageExchangeManager.getConfiguredImplementation ();
    aMEM.sendOutgoing (aRoutingInfo, aMessage);
  }
}
