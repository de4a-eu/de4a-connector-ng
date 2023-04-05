package com.helger.dcng.api.dd;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.collection.CollectionHelper;
import com.helger.peppolid.IDocumentTypeIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.peppolid.IProcessIdentifier;
import com.helger.peppolid.simple.process.SimpleProcessIdentifier;
import com.helger.xsds.bdxr.smp1.EndpointType;
import com.helger.xsds.bdxr.smp1.ProcessType;
import com.helger.xsds.bdxr.smp1.ServiceInformationType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataType;

/**
 * Helper interface to be used by the REST API.
 *
 * @author Philip Helger
 */
public interface IDDServiceMetadataProvider
{
  /**
   * Find the service metadata
   *
   * @param aParticipantID
   *        Participant ID to query. May not be <code>null</code>.
   * @param aDocTypeID
   *        Document type ID. May not be <code>null</code>.
   * @param aProcessID
   *        Process ID. May not be <code>null</code>.
   * @param sTransportProfile
   *        Transport profile ID. May not be <code>null</code>.
   * @return <code>null</code> if not found.
   */
  @Nullable
  ServiceMetadataType getServiceMetadata (@Nonnull IParticipantIdentifier aParticipantID,
                                          @Nonnull IDocumentTypeIdentifier aDocTypeID,
                                          @Nonnull IProcessIdentifier aProcessID,
                                          @Nonnull String sTransportProfile);

  /**
   * Find the dynamic discovery endpoint from the respective parameters.
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
   * @see #getServiceMetadata(IParticipantIdentifier, IDocumentTypeIdentifier,
   *      IProcessIdentifier, String)
   */
  @Nullable
  default EndpointType getEndpoint (@Nonnull final IParticipantIdentifier aParticipantID,
                                    @Nonnull final IDocumentTypeIdentifier aDocTypeID,
                                    @Nonnull final IProcessIdentifier aProcessID,
                                    @Nonnull final String sTransportProfile)
  {
    final ServiceMetadataType aSM = getServiceMetadata (aParticipantID, aDocTypeID, aProcessID, sTransportProfile);
    return getEndpoint (aSM, aProcessID, sTransportProfile);
  }

  /**
   * Find the dynamic discovery endpoint from the respective parameters.
   *
   * @param aSM
   *        The service metadata to be searched. May be <code>null</code>.
   * @param aProcessID
   *        Process ID. May not be <code>null</code>.
   * @param sTransportProfile
   *        Transport profile to be used. May not be <code>null</code>.
   * @return <code>null</code> if no such endpoint was found
   * @see #getServiceMetadata(IParticipantIdentifier, IDocumentTypeIdentifier,
   *      IProcessIdentifier, String)
   */
  @Nullable
  static EndpointType getEndpoint (@Nullable final ServiceMetadataType aSM,
                                   @Nonnull final IProcessIdentifier aProcessID,
                                   @Nonnull final String sTransportProfile)
  {
    if (aSM != null)
    {
      final ServiceInformationType aSI = aSM.getServiceInformation ();
      if (aSI != null)
      {
        final ProcessType aProcess = CollectionHelper.findFirst (aSI.getProcessList ().getProcess (),
                                                                 x -> aProcessID.hasSameContent (SimpleProcessIdentifier.wrap (x.getProcessIdentifier ())));
        if (aProcess != null)
        {
          return CollectionHelper.findFirst (aProcess.getServiceEndpointList ().getEndpoint (),
                                             x -> sTransportProfile.equals (x.getTransportProfile ()));
        }
      }
    }
    return null;
  }
}
