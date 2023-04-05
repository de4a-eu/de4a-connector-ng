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
