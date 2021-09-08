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
package com.helger.rdc.regrep;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.rdc.core.regrep.RdcRegRepHelper;
import com.helger.regrep.RegRep4Reader;
import com.helger.regrep.RegRep4Writer;
import com.helger.regrep.query.QueryRequest;
import com.helger.regrep.query.QueryResponse;

/**
 * Test class for class {@link RdcRegRepHelper}.
 *
 * @author Philip Helger
 */
public final class RdcRegRepHelperTest
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RdcRegRepHelperTest.class);

  @Test
  public void testRequest ()
  {
    final QueryRequest q = RdcRegRepHelper.wrapInQueryRequest ("dcid", "dcname", "pid");
    assertNotNull (q);
    final byte [] b = RegRep4Writer.queryRequest ().getAsBytes (q);
    assertNotNull (b);
    LOGGER.debug (RegRep4Writer.queryRequest ().setFormattedOutput (true).getAsString (q));

    // Read again
    final QueryRequest q2 = RegRep4Reader.queryRequest ().read (b);
    assertNotNull (q2);
  }

  @Test
  public void testResponse ()
  {
    final QueryResponse q = RdcRegRepHelper.wrapInQueryResponse ("dpid", "dpname");
    assertNotNull (q);
    final byte [] b = RegRep4Writer.queryResponse ().getAsBytes (q);
    assertNotNull (b);
    LOGGER.info (RegRep4Writer.queryResponse ().setFormattedOutput (true).getAsString (q));

    // Read again
    final QueryResponse q2 = RegRep4Reader.queryResponse ().read (b);
    assertNotNull (q2);
  }
}
