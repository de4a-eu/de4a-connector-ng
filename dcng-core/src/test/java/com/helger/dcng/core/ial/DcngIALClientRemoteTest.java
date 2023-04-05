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
package com.helger.dcng.core.ial;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.CommonsLinkedHashSet;

import eu.de4a.ial.api.jaxb.ResponseLookupRoutingInformationType;

/**
 * Test class for class {@link DcngIALClientRemote}.
 *
 * @author Philip Helger
 */
public final class DcngIALClientRemoteTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngIALClientRemoteTest.class);

  @Test
  public void testBasicNoATU ()
  {
    final DcngIALClientRemote aClient = DcngIALClientRemote.createDefaultInstance ();
    final ResponseLookupRoutingInformationType ret = aClient.queryIAL (new CommonsLinkedHashSet <> ("urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration:1.0"));
    assertNotNull (ret);
    LOGGER.info ("Result: " + ret);
    assertFalse (ret.hasErrorEntries ());
    assertTrue (ret.hasResponseItemEntries ());
  }

  @Test
  public void testBasicWithATU ()
  {
    final DcngIALClientRemote aClient = DcngIALClientRemote.createDefaultInstance ();
    final ResponseLookupRoutingInformationType ret = aClient.queryIAL (new CommonsLinkedHashSet <> ("urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration:1.0"),
                                                                       "AT1");
    assertNotNull (ret);
    LOGGER.info ("Result: " + ret);
    assertFalse (ret.hasErrorEntries ());
    assertTrue (ret.hasResponseItemEntries ());
  }

  @Test
  public void testBasicWithATUNotFound ()
  {
    final DcngIALClientRemote aClient = DcngIALClientRemote.createDefaultInstance ();
    final ResponseLookupRoutingInformationType ret = aClient.queryIAL (new CommonsLinkedHashSet <> ("urn:de4a-eu:CanonicalEvidenceType::CompanyRegistration:1.0"),
                                                                       "AT122");
    assertNotNull (ret);
    LOGGER.info ("Result: " + ret);
    assertTrue (ret.hasErrorEntries ());
    assertFalse (ret.hasResponseItemEntries ());
  }
}
