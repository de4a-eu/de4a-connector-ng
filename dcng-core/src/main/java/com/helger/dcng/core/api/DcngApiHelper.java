package com.helger.dcng.core.api;

import java.util.Locale;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.commons.error.level.EErrorLevel;
import com.helger.dcng.api.error.LoggingDcngErrorHandler;
import com.helger.dcng.api.me.IMessageExchangeSPI;
import com.helger.dcng.api.me.MessageExchangeManager;
import com.helger.dcng.api.me.model.MEMessage;
import com.helger.dcng.api.me.outgoing.IMERoutingInformation;
import com.helger.dcng.api.me.outgoing.MEOutgoingException;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;
import eu.de4a.kafkaclient.DE4AKafkaClient;

/**
 * A utility class that provides abstractions for all major tasks to be invoked
 * by the TC and is also used from the REST API components.
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngApiHelper
{
  public static final Locale DEFAULT_LOCALE = Locale.US;

  private DcngApiHelper ()
  {}

  @Nullable
  public static ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsOrderedSet <String> aCanonicalObjectTypeIDs)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO, () -> "Querying IAL for " + aCanonicalObjectTypeIDs);

    return DcngApiConfig.getIALClient ().queryIAL (aCanonicalObjectTypeIDs);
  }

  @Nullable
  public static ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsOrderedSet <String> aCanonicalObjectTypeIDs,
                                                               @Nonnull @Nonempty final String sATUCode)
  {
    DE4AKafkaClient.send (EErrorLevel.INFO,
                          () -> "Querying IAL for " + aCanonicalObjectTypeIDs + " in '" + sATUCode + "'");

    return DcngApiConfig.getIALClient ().queryIAL (aCanonicalObjectTypeIDs, sATUCode);
  }

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

    return DcngApiConfig.getDDServiceGroupHrefProvider ()
                        .getAllServiceGroupHrefs (aParticipantID, LoggingDcngErrorHandler.INSTANCE);
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

    final ServiceMetadataType ret = DcngApiConfig.getDDServiceMetadataProvider ()
                                                 .getServiceMetadata (aParticipantID,
                                                                      aDocTypeID,
                                                                      aProcessID,
                                                                      sTransportProfile);

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

    return DcngApiConfig.getDDServiceMetadataProvider ()
                        .getEndpoint (aParticipantID, aDocTypeID, aProcessID, sTransportProfile);
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
