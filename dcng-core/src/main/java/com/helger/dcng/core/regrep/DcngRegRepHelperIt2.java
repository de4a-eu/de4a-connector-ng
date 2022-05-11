/*
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
package com.helger.dcng.core.regrep;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.collection.CollectionHelper;
import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.io.IHasByteArray;
import com.helger.regrep.ERegRepResponseStatus;
import com.helger.regrep.RegRep4Reader;
import com.helger.regrep.RegRep4Writer;
import com.helger.regrep.RegRepHelper;
import com.helger.regrep.query.QueryRequest;
import com.helger.regrep.query.QueryResponse;
import com.helger.regrep.rim.AnyValueType;
import com.helger.regrep.rim.QueryType;
import com.helger.regrep.rim.RegistryObjectListType;
import com.helger.regrep.rim.RegistryObjectType;
import com.helger.regrep.rim.SlotType;
import com.helger.regrep.rim.ValueType;
import com.helger.regrep.slot.SlotBuilder;
import com.helger.xml.EXMLParserFeature;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;

/**
 * Create a RegRep request/response from existing messages.
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngRegRepHelperIt2
{
  private static final String QUERY_DEFINITION = "DE4AQueryIt2";
  private static final String QUERY_SLOT_NAME = "DE4AQuery";
  private static final Logger LOGGER = LoggerFactory.getLogger (DcngRegRepHelperIt2.class);

  private DcngRegRepHelperIt2 ()
  {}

  @Nonnull
  public static QueryRequest wrapInQueryRequest (@Nonnull final Element aPayload)
  {
    final QueryRequest ret = RegRepHelper.createEmptyQueryRequest ();
    ret.setId (UUID.randomUUID ().toString ());
    ret.getResponseOption ().setReturnType ("LeafClassWithRepositoryItem");
    // Value must match value from CIEM
    ret.addSlot (new SlotBuilder ().setName ("SpecificationIdentifier").setValue ("de4a-iem-v2").build ());
    ret.addSlot (new SlotBuilder ().setName ("IssueDateTime").setValue (PDTFactory.getCurrentLocalDateTime ()).build ());
    {
      final QueryType aQuery = new QueryType ();
      aQuery.setQueryDefinition (QUERY_DEFINITION);
      aQuery.addSlot (new SlotBuilder ().setName (QUERY_SLOT_NAME).setValue (aPayload).build ());
      ret.setQuery (aQuery);
    }
    return ret;
  }

  @Nonnull
  public static QueryResponse wrapInQueryResponse (final String sRequestID, final Element aPayload)
  {
    final QueryResponse ret = RegRepHelper.createEmptyQueryResponse (ERegRepResponseStatus.SUCCESS);
    ret.setRequestId (sRequestID);
    ret.addSlot (new SlotBuilder ().setName ("SpecificationIdentifier").setValue ("de4a-iem-v2").build ());
    ret.addSlot (new SlotBuilder ().setName ("IssueDateTime").setValue (PDTFactory.getCurrentLocalDateTime ()).build ());

    {
      final RegistryObjectListType aROList = new RegistryObjectListType ();
      final RegistryObjectType aRO = new RegistryObjectType ();
      aRO.setId (UUID.randomUUID ().toString ());
      aRO.addSlot (new SlotBuilder ().setName ("DE4AResponse").setValue (aPayload).build ());
      aROList.addRegistryObject (aRO);
      ret.setRegistryObjectList (aROList);
    }
    return ret;
  }

  @Nonnull
  public static byte [] wrapInRegRep (final boolean bIsRequest, @Nonnull final byte [] aXMLBytes)
  {
    ValueEnforcer.notNull (aXMLBytes, "XMLBytes");

    final Document aDoc = DOMReader.readXMLDOM (aXMLBytes, new DOMReaderSettings ().setFeatureValues (EXMLParserFeature.AVOID_XML_ATTACKS));
    if (aDoc == null)
      throw new IllegalStateException ("Failed to parse payload as XML");

    LOGGER.info ("Wrapping object into RegRep It2 " + (bIsRequest ? "Request" : "Response"));

    final byte [] aRegRepPayload;
    if (bIsRequest)
    {
      // Currently everything is a request
      final QueryRequest aRRReq = wrapInQueryRequest (aDoc.getDocumentElement ());
      aRegRepPayload = RegRep4Writer.queryRequest ().setFormattedOutput (true).getAsBytes (aRRReq);
    }
    else
    {
      final QueryResponse aRRResp = wrapInQueryResponse ("TODO", aDoc.getDocumentElement ());
      aRegRepPayload = RegRep4Writer.queryResponse ().setFormattedOutput (true).getAsBytes (aRRResp);
    }
    return aRegRepPayload;
  }

  @Nullable
  public static Element extractPayload (@Nonnull final IHasByteArray aData)
  {
    final QueryRequest aQuery = RegRep4Reader.queryRequest ().read (aData.bytes (), aData.getOffset (), aData.size ());
    if (aQuery != null)
      return extractPayload (aQuery);

    LOGGER.error ("The provided bytes could not be interpreted to a supported RegRep object.");
    return null;
  }

  @Nullable
  public static Element extractPayload (@Nonnull final QueryRequest aQueryRequest)
  {
    ValueEnforcer.notNull (aQueryRequest, "QueryRequest");

    final QueryType aQuery = aQueryRequest.getQuery ();
    if (aQuery != null)
    {
      if (QUERY_DEFINITION.equals (aQuery.getQueryDefinition ()))
      {
        final SlotType aSlot = CollectionHelper.findFirst (aQuery.getSlot (), x -> QUERY_SLOT_NAME.equals (x.getName ()));
        if (aSlot != null)
        {
          final ValueType aSlotValue = aSlot.getSlotValue ();
          if (aSlotValue instanceof AnyValueType)
          {
            final Object aAny = ((AnyValueType) aSlotValue).getAny ();
            if (aAny instanceof Element)
            {
              return (Element) aAny;
            }

            LOGGER.error ("Provided RegRep Query element contains a slot with name '" +
                          QUERY_SLOT_NAME +
                          "' that has an unsupported AnyValue content.");
          }
          else
            LOGGER.error ("Provided RegRep Query element contains a slot with name '" +
                          QUERY_SLOT_NAME +
                          "' that has the wrong value type.");
        }
        else
          LOGGER.error ("Provided RegRep Query element does not contain a slot with name '" + QUERY_SLOT_NAME + "'.");
      }
      else
        LOGGER.error ("Provided RegRep Query element uses the wrong Query definition '" +
                      aQuery.getQueryDefinition () +
                      ". Was expecting '" +
                      QUERY_DEFINITION +
                      "'.");
    }
    else
      LOGGER.error ("Provided RegRep query has no 'Query' element");
    return null;
  }
}
