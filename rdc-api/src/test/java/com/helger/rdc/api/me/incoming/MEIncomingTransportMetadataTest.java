/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
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
package com.helger.rdc.api.me.incoming;

import org.junit.Test;

import com.helger.commons.mock.CommonsTestHelper;
import com.helger.peppolid.factory.IIdentifierFactory;
import com.helger.rdc.api.RDCConfig;

/**
 * Test class for class {@link MEIncomingTransportMetadata}.
 *
 * @author Philip Helger
 */
public final class MEIncomingTransportMetadataTest
{
  @Test
  public void testEqualsHashcode ()
  {
    final IIdentifierFactory aIF = RDCConfig.getIdentifierFactory ();

    MEIncomingTransportMetadata m = new MEIncomingTransportMetadata (null, null, null, null);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m, new MEIncomingTransportMetadata (null, null, null, null));

    m = new MEIncomingTransportMetadata (aIF.createParticipantIdentifierWithDefaultScheme ("bla"), null, null, null);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m,
                                                                       new MEIncomingTransportMetadata (aIF.createParticipantIdentifierWithDefaultScheme ("bla"),
                                                                                                        null,
                                                                                                        null,
                                                                                                        null));

    m = new MEIncomingTransportMetadata (null, aIF.createParticipantIdentifierWithDefaultScheme ("bla"), null, null);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m,
                                                                       new MEIncomingTransportMetadata (null,
                                                                                                        aIF.createParticipantIdentifierWithDefaultScheme ("bla"),
                                                                                                        null,
                                                                                                        null));

    m = new MEIncomingTransportMetadata (null, null, aIF.createDocumentTypeIdentifierWithDefaultScheme ("foo"), null);
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m,
                                                                       new MEIncomingTransportMetadata (null,
                                                                                                        null,
                                                                                                        aIF.createDocumentTypeIdentifierWithDefaultScheme ("foo"),
                                                                                                        null));

    m = new MEIncomingTransportMetadata (null, null, null, aIF.createProcessIdentifierWithDefaultScheme ("proc"));
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m,
                                                                       new MEIncomingTransportMetadata (null,
                                                                                                        null,
                                                                                                        null,
                                                                                                        aIF.createProcessIdentifierWithDefaultScheme ("proc")));

    m = new MEIncomingTransportMetadata (aIF.createParticipantIdentifierWithDefaultScheme ("bla"),
                                         aIF.createParticipantIdentifierWithDefaultScheme ("bla2"),
                                         aIF.createDocumentTypeIdentifierWithDefaultScheme ("foo"),
                                         aIF.createProcessIdentifierWithDefaultScheme ("proc"));
    CommonsTestHelper.testDefaultImplementationWithEqualContentObject (m,
                                                                       new MEIncomingTransportMetadata (aIF.createParticipantIdentifierWithDefaultScheme ("bla"),
                                                                                                        aIF.createParticipantIdentifierWithDefaultScheme ("bla2"),
                                                                                                        aIF.createDocumentTypeIdentifierWithDefaultScheme ("foo"),
                                                                                                        aIF.createProcessIdentifierWithDefaultScheme ("proc")));
  }
}
