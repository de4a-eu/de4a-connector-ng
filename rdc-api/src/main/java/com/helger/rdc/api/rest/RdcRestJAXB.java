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
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

import com.helger.commons.ValueEnforcer;
import com.helger.commons.annotation.ReturnsMutableCopy;
import com.helger.commons.collection.impl.CommonsArrayList;
import com.helger.commons.collection.impl.ICommonsList;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.jaxb.GenericJAXBMarshaller;
import com.helger.peppolid.IIdentifier;

/**
 * JAXB helper for TC NG REST classes.
 *
 * @author Philip Helger
 */
@Immutable
public final class RdcRestJAXB
{
  public static final ClassPathResource XSD_RES = new ClassPathResource ("/schemas/rdc-rest.xsd",
                                                                         RdcRestJAXB.class.getClassLoader ());
  public static final String NS_URI = "urn:com.helger/de4a/connector/exchange/2021/05/";
  public static final String DEFAULT_NAMESPACE_PREFIX = "de4a";

  private RdcRestJAXB ()
  {}

  @Nonnull
  @ReturnsMutableCopy
  public static ICommonsList <ClassPathResource> getAllXSDResources ()
  {
    final ICommonsList <ClassPathResource> ret = new CommonsArrayList <> ();
    ret.add (XSD_RES);
    return ret;
  }

  /**
   * @return A new marshaller to read and write {@link TCOutgoingMessage}
   *         objects. Never <code>null</code>.
   */
  @Nonnull
  public static GenericJAXBMarshaller <TCOutgoingMessage> outgoingMessage ()
  {
    final GenericJAXBMarshaller <TCOutgoingMessage> ret = new GenericJAXBMarshaller <> (TCOutgoingMessage.class,
                                                                                        getAllXSDResources (),
                                                                                        new ObjectFactory ()::createOutgoingMessage);
    ret.setFormattedOutput (true);
    ret.setNamespaceContext (RdcRestNamespaceContext.getInstance ());
    return ret;
  }

  /**
   * @return A new marshaller to read and write {@link TCIncomingMessage}
   *         objects. Never <code>null</code>.
   */
  @Nonnull
  public static GenericJAXBMarshaller <TCIncomingMessage> incomingMessage ()
  {
    final GenericJAXBMarshaller <TCIncomingMessage> ret = new GenericJAXBMarshaller <> (TCIncomingMessage.class,
                                                                                        getAllXSDResources (),
                                                                                        new ObjectFactory ()::createIncomingMessage);
    ret.setFormattedOutput (true);
    ret.setNamespaceContext (RdcRestNamespaceContext.getInstance ());
    return ret;
  }

  /**
   * @param aID
   *        The source identifier. May not be <code>null</code>.
   * @return The created {@link TCIdentifierType} and never <code>null</code>.
   */
  @Nonnull
  public static TCIdentifierType createTCID (@Nonnull final IIdentifier aID)
  {
    ValueEnforcer.notNull (aID, "ID");
    return createTCID (aID.getScheme (), aID.getValue ());
  }

  /**
   * Create a new {@link TCIdentifierType}
   *
   * @param sScheme
   *        The scheme to use. May be <code>null</code>.
   * @param sValue
   *        The value to use. May not be <code>null</code>.
   * @return The created {@link TCIdentifierType} and never <code>null</code>.
   */
  @Nonnull
  public static TCIdentifierType createTCID (@Nullable final String sScheme, @Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sValue, "Value");
    final TCIdentifierType ret = new TCIdentifierType ();
    ret.setScheme (sScheme);
    ret.setValue (sValue);
    return ret;
  }
}
