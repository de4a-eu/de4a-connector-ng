package com.helger.dcng.core;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

/**
 * Test class for class {@link CDcngVersion}
 *
 * @author Philip Helger
 */
public final class CDcngVersionTest
{
  @Test
  public void testBasic ()
  {
    assertNotEquals ("undefined", CDcngVersion.BUILD_VERSION);
    assertNotEquals ("undefined", CDcngVersion.BUILD_TIMESTAMP);
  }
}
