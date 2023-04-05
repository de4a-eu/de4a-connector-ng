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
