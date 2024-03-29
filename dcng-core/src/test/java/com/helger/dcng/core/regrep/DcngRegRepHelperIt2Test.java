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
package com.helger.dcng.core.regrep;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.helger.regrep.RegRep4Reader;
import com.helger.regrep.RegRep4Writer;
import com.helger.regrep.query.QueryRequest;
import com.helger.regrep.query.QueryResponse;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Test class for class {@link DcngRegRepHelperIt2}.
 *
 * @author Philip Helger
 */
public final class DcngRegRepHelperIt2Test
{
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngRegRepHelperIt2Test.class);

  @Test
  public void testRequest ()
  {
    final Element e = DOMReader.readXMLDOM ("<De4aRequest />").getDocumentElement ();

    final QueryRequest q = DcngRegRepHelperIt2.wrapInQueryRequest (e);
    assertNotNull (q);
    final byte [] b = RegRep4Writer.queryRequest ().getAsBytes (q);
    assertNotNull (b);

    if (false)
      LOGGER.info (RegRep4Writer.queryRequest ().setFormattedOutput (true).getAsString (q));

    // Read again
    final QueryRequest q2 = RegRep4Reader.queryRequest ().read (b);
    assertNotNull (q2);
  }

  @Test
  public void testResponse ()
  {
    final Element e = DOMReader.readXMLDOM ("<De4aResponse />").getDocumentElement ();

    final QueryResponse q = DcngRegRepHelperIt2.wrapInQueryResponse ("reqid", e);
    assertNotNull (q);
    final byte [] b = RegRep4Writer.queryResponse ().getAsBytes (q);
    assertNotNull (b);

    if (false)
      LOGGER.info (RegRep4Writer.queryResponse ().setFormattedOutput (true).getAsString (q));

    // Read again
    final QueryResponse q2 = RegRep4Reader.queryResponse ().read (b);
    assertNotNull (q2);
  }
}
