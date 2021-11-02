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
package com.helger.dcng.api.rest;

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
 * JAXB helper for DCNG REST classes.
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngRestJAXB
{
  public static final ClassPathResource XSD_RES = new ClassPathResource ("/schemas/dcng-rest.xsd", DcngRestJAXB.class.getClassLoader ());
  public static final String NS_URI = "urn:com.helger/de4a/connector/exchange/2021/05/";
  public static final String DEFAULT_NAMESPACE_PREFIX = "de4a";

  private DcngRestJAXB ()
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
   * @return A new marshaller to read and write {@link DCNGOutgoingMessage}
   *         objects. Never <code>null</code>.
   */
  @Nonnull
  public static GenericJAXBMarshaller <DCNGOutgoingMessage> outgoingMessage ()
  {
    final GenericJAXBMarshaller <DCNGOutgoingMessage> ret = new GenericJAXBMarshaller <> (DCNGOutgoingMessage.class,
                                                                                          getAllXSDResources (),
                                                                                          new ObjectFactory ()::createOutgoingMessage);
    ret.setFormattedOutput (true);
    ret.setNamespaceContext (DcngRestNamespaceContext.getInstance ());
    return ret;
  }

  /**
   * @return A new marshaller to read and write {@link DCNGIncomingMessage}
   *         objects. Never <code>null</code>.
   */
  @Nonnull
  public static GenericJAXBMarshaller <DCNGIncomingMessage> incomingMessage ()
  {
    final GenericJAXBMarshaller <DCNGIncomingMessage> ret = new GenericJAXBMarshaller <> (DCNGIncomingMessage.class,
                                                                                          getAllXSDResources (),
                                                                                          new ObjectFactory ()::createIncomingMessage);
    ret.setFormattedOutput (true);
    ret.setNamespaceContext (DcngRestNamespaceContext.getInstance ());
    return ret;
  }

  /**
   * @param aID
   *        The source identifier. May not be <code>null</code>.
   * @return The created {@link DCNGIdentifierType} and never <code>null</code>.
   */
  @Nonnull
  public static DCNGIdentifierType createDCNGID (@Nonnull final IIdentifier aID)
  {
    ValueEnforcer.notNull (aID, "ID");
    return createDCNGID (aID.getScheme (), aID.getValue ());
  }

  /**
   * Create a new {@link DCNGIdentifierType}
   *
   * @param sScheme
   *        The scheme to use. May be <code>null</code>.
   * @param sValue
   *        The value to use. May not be <code>null</code>.
   * @return The created {@link DCNGIdentifierType} and never <code>null</code>.
   */
  @Nonnull
  public static DCNGIdentifierType createDCNGID (@Nullable final String sScheme, @Nonnull final String sValue)
  {
    ValueEnforcer.notNull (sValue, "Value");
    final DCNGIdentifierType ret = new DCNGIdentifierType ();
    ret.setScheme (sScheme);
    ret.setValue (sValue);
    return ret;
  }
}
