package com.helger.dcng.api.dd;

import javax.annotation.Nonnull;

import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.dcng.api.error.IDcngErrorHandler;
import com.helger.peppolid.IParticipantIdentifier;

/**
 * Helper interface to be used by the REST API.
 *
 * @author Philip Helger
 */
public interface IDDServiceGroupHrefProvider
{
  /**
   * @param aParticipantID
   *        Participant ID to query.
   * @param aErrorHandler
   *        The error handler to be used. May not be <code>null</code>.
   * @return A non-<code>null</code> sorted map of all hrefs. The key MUST be
   *         URL decoded whereas the value is the "original href" as found in
   *         the response.
   */
  @Nonnull
  ICommonsSortedMap <String, String> getAllServiceGroupHrefs (@Nonnull IParticipantIdentifier aParticipantID,
                                                              @Nonnull IDcngErrorHandler aErrorHandler);
}
