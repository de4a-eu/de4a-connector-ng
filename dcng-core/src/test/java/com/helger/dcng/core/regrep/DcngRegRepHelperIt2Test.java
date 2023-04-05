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
