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
