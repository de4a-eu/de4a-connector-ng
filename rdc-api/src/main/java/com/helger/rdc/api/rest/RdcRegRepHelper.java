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
package com.helger.rdc.api.rest;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.helger.commons.datetime.PDTFactory;
import com.helger.regrep.ERegRepResponseStatus;
import com.helger.regrep.RegRepHelper;
import com.helger.regrep.query.QueryRequest;
import com.helger.regrep.query.QueryResponse;
import com.helger.regrep.rim.QueryType;
import com.helger.regrep.rim.RegistryObjectListType;
import com.helger.regrep.rim.RegistryObjectType;
import com.helger.regrep.slot.ERegRepCollectionType;
import com.helger.regrep.slot.SlotBuilder;
import com.helger.regrep.slot.SlotHelper;
import com.helger.xml.XMLFactory;
import com.helger.xml.serialize.read.DOMReader;

/**
 * Create a RegRep request/response from existing messages.
 *
 * @author Philip Helger
 */
@Immutable
public final class RdcRegRepHelper
{
  private RdcRegRepHelper ()
  {}

  @Nonnull
  private static Element _createAgent (final String sID, final String sName)
  {
    final Document d = XMLFactory.newDocument ();
    final Element eAgent = (Element) d.appendChild (d.createElementNS ("https://semic.org/sa/cv/cagv/agent-2.0.0#", "Agent"));
    final Element eID = (Element) eAgent.appendChild (d.createElementNS ("https://data.europe.eu/semanticassets/ns/cv/common/cbc_v2.0.0#",
                                                                         "id"));
    eID.setAttribute ("schemeID", "EIDAS");
    eID.appendChild (d.createTextNode (sID));
    final Element eName = (Element) eAgent.appendChild (d.createElementNS ("https://data.europe.eu/semanticassets/ns/cv/common/cbc_v2.0.0#",
                                                                           "name"));
    eName.appendChild (d.createTextNode (sName));
    return eAgent;
  }

  @Nonnull
  private static Element _createCorePerson (final String sPersonID)
  {
    final Document d = XMLFactory.newDocument ();
    final Element eCorePerson = (Element) d.appendChild (d.createElementNS ("http://www.w3.org/ns/corevocabulary/AggregateComponents",
                                                                            "CorePerson"));
    final Element ePersonID = (Element) eCorePerson.appendChild (d.createElementNS ("http://www.w3.org/ns/corevocabulary/BasicComponents",
                                                                                    "PersonID"));
    ePersonID.setAttribute ("schemeID", "EIDAS");
    ePersonID.appendChild (d.createTextNode (sPersonID));
    eCorePerson.appendChild (d.createElementNS ("http://www.w3.org/ns/corevocabulary/BasicComponents", "PersonFamilyName"))
               .appendChild (d.createTextNode ("XXXX"));
    eCorePerson.appendChild (d.createElementNS ("http://www.w3.org/ns/corevocabulary/BasicComponents", "PersonGivenName"))
               .appendChild (d.createTextNode ("ZZZZZ"));
    eCorePerson.appendChild (d.createElementNS ("http://www.w3.org/ns/corevocabulary/BasicComponents", "PersonBirthDate"))
               .appendChild (d.createTextNode ("1900-01-01"));
    return eCorePerson;
  }

  @Nonnull
  private static Element _createConcept ()
  {
    return DOMReader.readXMLDOM ("<cccev:concept xmlns:cccev=\"https://data.europe.eu/semanticassets/ns/cv/cccev_v2.0.0#\"" +
                                 " xmlns:cbc=\"https://data.europe.eu/semanticassets/ns/cv/common/cbc_v2.0.0#\"" +
                                 " xmlns:toop=\"http://toop.eu/registered-organization\">" +
                                 "<cbc:id>ConceptID-1</cbc:id>" +
                                 "<cbc:qName>toop:CompanyData</cbc:qName>" +
                                 "</cccev:concept>")
                    .getDocumentElement ();
  }

  @Nonnull
  public static QueryRequest wrapInQueryRequest (final String sDCID, final String sDCName, final String sPersonID)
  {
    final QueryRequest ret = RegRepHelper.createEmptyQueryRequest ();
    ret.setId ("c4369c4d-740e-4b64-80f0-7b209a66d629");
    ret.getResponseOption ().setReturnType ("LeafClassWithRepositoryItem");
    ret.addSlot (new SlotBuilder ().setName ("SpecificationIdentifier").setValue ("toop-edm:v2.1").build ());
    ret.addSlot (new SlotBuilder ().setName ("IssueDateTime").setValue (PDTFactory.getCurrentLocalDateTime ()).build ());
    ret.addSlot (new SlotBuilder ().setName ("DataConsumer").setValue (_createAgent (sDCID, sDCName)).build ());
    {
      final QueryType aQuery = new QueryType ();
      aQuery.setQueryDefinition ("ConceptQuery");
      aQuery.addSlot (new SlotBuilder ().setName ("NaturalPerson").setValue (_createCorePerson (sPersonID)).build ());
      aQuery.addSlot (new SlotBuilder ().setName ("ConceptRequestList")
                                        .setValue (ERegRepCollectionType.SET, SlotHelper.createSlotValue (_createConcept ()))
                                        .build ());
      ret.setQuery (aQuery);
    }
    return ret;
  }

  @Nonnull
  public static QueryResponse wrapInQueryResponse (final String sDPID, final String sDPName)
  {
    final QueryResponse ret = RegRepHelper.createEmptyQueryResponse (ERegRepResponseStatus.SUCCESS);
    ret.setRequestId ("c4369c4d-740e-4b64-80f0-7b209a66d629");
    ret.addSlot (new SlotBuilder ().setName ("SpecificationIdentifier").setValue ("toop-edm:v2.1").build ());
    ret.addSlot (new SlotBuilder ().setName ("IssueDateTime").setValue (PDTFactory.getCurrentLocalDateTime ()).build ());
    ret.addSlot (new SlotBuilder ().setName ("DataProvider").setValue (_createAgent (sDPID, sDPName)).build ());

    {
      final RegistryObjectListType aROList = new RegistryObjectListType ();
      final RegistryObjectType aRO = new RegistryObjectType ();
      aRO.setId ("341341341-740e-4b64-80f0-3153513529");
      aRO.addSlot (new SlotBuilder ().setName ("ConceptValues")
                                     .setValue (ERegRepCollectionType.SET, SlotHelper.createSlotValue (_createConcept ()))
                                     .build ());
      aROList.addRegistryObject (aRO);
      ret.setRegistryObjectList (aROList);
    }
    return ret;
  }
}
