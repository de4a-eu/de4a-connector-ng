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
package com.helger.dcng.api.rest;

import javax.annotation.Nonnull;

import com.helger.commons.annotation.Singleton;
import com.helger.xml.namespace.MapBasedNamespaceContext;

/**
 * The namespace context to be used as the namespace prefix mapper.
 *
 * @author Philip Helger
 */
@Singleton
public class DcngRestNamespaceContext extends MapBasedNamespaceContext
{
  private static final class SingletonHolder
  {
    static final DcngRestNamespaceContext INSTANCE = new DcngRestNamespaceContext ();
  }

  protected DcngRestNamespaceContext ()
  {
    addMapping (DcngRestJAXB.DEFAULT_NAMESPACE_PREFIX, DcngRestJAXB.NS_URI);
  }

  @Nonnull
  public static DcngRestNamespaceContext getInstance ()
  {
    return SingletonHolder.INSTANCE;
  }
}
