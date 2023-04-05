package com.helger.dcng.api.ial;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.commons.annotation.Nonempty;
import com.helger.commons.collection.impl.ICommonsOrderedSet;
import com.helger.commons.error.list.ErrorList;

import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;

/**
 * Generic IAL client API.
 *
 * @author Philip Helger
 * @since 0.2.4
 */
public interface IIALClient
{
  /**
   * Query the IAL for canonical object types without a specific ATU code.
   *
   * @param aCanonicalObjectTypeIDs
   *        The IDs to query. May neither be <code>null</code> nor empty.
   * @return <code>null</code> in case the IAL could not be queried.
   */
  @Nullable
  default ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsOrderedSet <String> aCanonicalObjectTypeIDs)
  {
    return queryIAL (aCanonicalObjectTypeIDs, null);
  }

  /**
   * Query the IAL for canonical object types with a specific ATU code.
   *
   * @param aCanonicalObjectTypeIDs
   *        The IDs to query. May neither be <code>null</code> nor empty.
   * @param sATUCode
   *        The ATU code to query. May be <code>null</code> or empty.
   * @return <code>null</code> in case the IAL could not be queried.
   */
  @Nullable
  default ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty final ICommonsOrderedSet <String> aCanonicalObjectTypeIDs,
                                                         @Nullable final String sATUCode)
  {
    return queryIAL (aCanonicalObjectTypeIDs, sATUCode, new ErrorList ());
  }

  /**
   * Query the IAL for canonical object types with a specific ATU code.
   *
   * @param aCanonicalObjectTypeIDs
   *        The IDs to query. May neither be <code>null</code> nor empty.
   * @param sATUCode
   *        The ATU code to query. May be <code>null</code> or empty.
   * @param aErrorList
   *        The error list to be filled. May not be <code>null</code>.
   * @return <code>null</code> in case the IAL could not be queried.
   */
  @Nullable
  ResponseLookupRoutingInformationType queryIAL (@Nonnull @Nonempty ICommonsOrderedSet <String> aCanonicalObjectTypeIDs,
                                                 @Nullable String sATUCode,
                                                 @Nonnull ErrorList aErrorList);
}
