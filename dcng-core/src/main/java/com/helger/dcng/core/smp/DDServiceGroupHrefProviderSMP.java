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
package com.helger.dcng.core.smp;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.impl.CommonsTreeMap;
import com.helger.commons.collection.impl.ICommonsSortedMap;
import com.helger.dcng.api.dd.IDDServiceGroupHrefProvider;
import com.helger.dcng.api.error.EDcngErrorCode;
import com.helger.dcng.api.error.IDcngErrorHandler;
import com.helger.peppolid.CIdentifier;
import com.helger.peppolid.IParticipantIdentifier;
import com.helger.smpclient.bdxr1.IBDXRServiceGroupProvider;
import com.helger.smpclient.exception.SMPClientException;
import com.helger.smpclient.url.SMPDNSResolutionException;
import com.helger.xsds.bdxr.smp1.ServiceGroupType;
import com.helger.xsds.bdxr.smp1.ServiceMetadataReferenceType;

public class DDServiceGroupHrefProviderSMP extends AbstractDDClient implements IDDServiceGroupHrefProvider
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DDServiceGroupHrefProviderSMP.class);

  public DDServiceGroupHrefProviderSMP ()
  {}

  @Nonnull
  public ICommonsSortedMap <String, String> getAllServiceGroupHrefs (@Nonnull final IParticipantIdentifier aParticipantID,
                                                                     @Nonnull final IDcngErrorHandler aErrorHandler)
  {
    ValueEnforcer.notNull (aParticipantID, "ParticipantID");
    ValueEnforcer.notNull (aErrorHandler, "ErrorHandler");

    try
    {
      final ICommonsSortedMap <String, String> ret = new CommonsTreeMap <> ();
      final IBDXRServiceGroupProvider aClient = getServiceGroupProvider (aParticipantID);

      // Get all HRefs and sort them by decoded URL
      final ServiceGroupType aSG = aClient.getServiceGroupOrNull (aParticipantID);

      // Map from cleaned URL to original URL
      if (aSG != null && aSG.getServiceMetadataReferenceCollection () != null)
      {
        for (final ServiceMetadataReferenceType aSMR : aSG.getServiceMetadataReferenceCollection ().getServiceMetadataReference ())
        {
          // Decoded href is important for unification
          final String sHref = CIdentifier.createPercentDecoded (aSMR.getHref ());
          if (ret.put (sHref, aSMR.getHref ()) != null)
            aErrorHandler.onWarning ("The SMP ServiceGroup list contains the duplicate URL '" + sHref + "'", EDcngErrorCode.GEN);
        }
      }
      return ret;
    }
    catch (final SMPDNSResolutionException | SMPClientException ex)
    {
      LOGGER.error ("getServiceMetadata exception: " + ex.getClass ().getName () + " - " + ex.getMessage ());
      throw new IllegalStateException (ex);
    }
  }
}
