package com.helger.dcng;

import org.junit.Test;

import com.helger.commons.mock.SPITestHelper;
import com.helger.dcng.api.DcngConfig;
import com.helger.photon.core.mock.PhotonCoreValidator;

/**
 * Test SPI definitions and web.xml
 *
 * @author Philip Helger
 */
public final class SPITest
{
  @Test
  public void testBasic () throws Exception
  {
    SPITestHelper.testIfAllSPIImplementationsAreValid ();
    PhotonCoreValidator.validateExternalResources ();
  }

  @Test
  public void testConfig () throws Exception
  {
    // Required to test the ph-config bug when building on the commandline
    DcngConfig.Tracker.isTrackerEnabled ();
  }
}
