package com.helger.rdc.api.rest;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    assertNotNull (RegRep4Writer.queryRequest ().getAsBytes (q));
    LOGGER.debug (RegRep4Writer.queryRequest ().setFormattedOutput (true).getAsString (q));
  }

  @Test
  public void testResponse ()
  {
    final QueryResponse q = RdcRegRepHelper.wrapInQueryResponse ("dpid", "dpname");
    assertNotNull (q);
    assertNotNull (RegRep4Writer.queryResponse ().getAsBytes (q));
    LOGGER.info (RegRep4Writer.queryResponse ().setFormattedOutput (true).getAsString (q));
  }
}
