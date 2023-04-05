package com.helger.dcng.api;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * Test class for class {@link DcngConfig}.
 *
 * @author Philip Helger
 */
public final class DcngConfigTest
{
  @Test
  public void testBasic ()
  {
    assertFalse (DcngConfig.SMP.isUseDNS ());
    assertNotNull (DcngConfig.getIdentifierFactory ());
  }
}
