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
package com.helger.rdc.core.validation;

import java.util.Locale;

import javax.annotation.Nonnull;

import org.w3c.dom.Document;

import com.helger.commons.error.list.ErrorList;
import com.helger.commons.io.resource.inmemory.ReadableResourceByteArray;
import com.helger.phive.api.EValidationType;
import com.helger.phive.api.artefact.ValidationArtefact;
import com.helger.phive.api.execute.ValidationExecutionManager;
import com.helger.phive.api.executorset.IValidationExecutorSet;
import com.helger.phive.api.executorset.VESID;
import com.helger.phive.api.executorset.ValidationExecutorSetRegistry;
import com.helger.phive.api.result.ValidationResult;
import com.helger.phive.api.result.ValidationResultList;
import com.helger.phive.engine.source.IValidationSourceXML;
import com.helger.phive.engine.source.ValidationSourceXML;
import com.helger.rdc.api.validation.IVSValidator;
import com.helger.xml.EXMLParserFeature;
import com.helger.xml.sax.WrappedCollectingSAXErrorHandler;
import com.helger.xml.serialize.read.DOMReader;
import com.helger.xml.serialize.read.DOMReaderSettings;

/**
 * The default implementation of {@link IVSValidator}
 *
 * @author Philip Helger
 */
public class RDCValidator implements IVSValidator
{
  private static final ValidationExecutorSetRegistry <IValidationSourceXML> VER = new ValidationExecutorSetRegistry <> ();
  static
  {
    // Init all TOOP rules
    RDCValidationRules.initRDC (VER);
  }

  @Nonnull
  public static ValidationExecutorSetRegistry <IValidationSourceXML> internalGetRegistry ()
  {
    return VER;
  }

  @Nonnull
  public static IValidationExecutorSet <IValidationSourceXML> getVES (@Nonnull final VESID aVESID)
  {
    final IValidationExecutorSet <IValidationSourceXML> aVES = VER.getOfID (aVESID);
    if (aVES == null)
      throw new IllegalStateException ("Unexpected VESID '" + aVESID.getAsSingleID () + "'");
    return aVES;
  }

  public RDCValidator ()
  {}

  @Nonnull
  public ValidationResultList validate (@Nonnull final VESID aVESID,
                                        @Nonnull final byte [] aPayload,
                                        @Nonnull final Locale aDisplayLocale)
  {
    final ErrorList aXMLErrors = new ErrorList ();
    final ValidationResultList aValidationResultList = new ValidationResultList ();

    final ReadableResourceByteArray aXMLRes = new ReadableResourceByteArray (aPayload);
    final Document aDoc = DOMReader.readXMLDOM (aXMLRes,
                                                new DOMReaderSettings ().setErrorHandler (new WrappedCollectingSAXErrorHandler (aXMLErrors))
                                                                        .setLocale (aDisplayLocale)
                                                                        .setFeatureValues (EXMLParserFeature.AVOID_XML_ATTACKS));
    if (aDoc != null)
    {
      // What to validate?
      final IValidationSourceXML aValidationSource = ValidationSourceXML.create ("uploaded content",
                                                                                 DOMReader.readXMLDOM (aPayload));
      // Start validation
      ValidationExecutionManager.executeValidation (getVES (aVESID),
                                                    aValidationSource,
                                                    aValidationResultList,
                                                    aDisplayLocale);
    }

    // Add all XML parsing stuff - always first item
    // Also add if no error is present to have it shown in the list
    aValidationResultList.add (0,
                               new ValidationResult (new ValidationArtefact (EValidationType.XML, aXMLRes),
                                                     aXMLErrors));
    return aValidationResultList;
  }
}
