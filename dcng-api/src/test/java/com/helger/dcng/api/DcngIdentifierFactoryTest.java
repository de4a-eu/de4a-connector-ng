package com.helger.dcng.api;

import static org.junit.Assert.assertNotEquals;

import org.junit.Test;

import com.helger.peppolid.IParticipantIdentifier;

/**
 * Test class for class {@link DcngIdentifierFactory}.
 *
 * @author Philip Helger
 */
public final class DcngIdentifierFactoryTest
{
  @Test
  public void testBasic ()
  {
    final DcngIdentifierFactory aIF = DcngIdentifierFactory.INSTANCE;

    final IParticipantIdentifier aPI1 = aIF.createParticipantIdentifier (null, "iso6523-actorid-upis::9999:elonia");
    final IParticipantIdentifier aPI2 = aIF.createParticipantIdentifier (DcngIdentifierFactory.PARTICIPANT_SCHEME, "9999:elonia");
    assertNotEquals (aPI1.getURIEncoded (), aPI2.getURIEncoded ());
  }
}
