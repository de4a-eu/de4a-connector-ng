package com.helger.dcng.core;

import javax.annotation.concurrent.Immutable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.collection.impl.ICommonsMap;
import com.helger.commons.io.resource.ClassPathResource;
import com.helger.commons.lang.PropertiesHelper;

/**
 * Contains application wide constants.
 *
 * @author Philip Helger
 */
@Immutable
public final class CDcngVersion
{
  /** Current version - from properties file */
  public static final String BUILD_VERSION;
  /** Build timestamp - from properties file */
  public static final String BUILD_TIMESTAMP;

  private static final Logger LOGGER = LoggerFactory.getLogger (CDcngVersion.class);

  static
  {
    String sProjectVersion = null;
    String sProjectTimestamp = null;
    final ICommonsMap <String, String> p = PropertiesHelper.loadProperties (new ClassPathResource ("dcng-version.properties",
                                                                                                   CDcngVersion.class.getClassLoader ()));
    if (p != null)
    {
      sProjectVersion = p.get ("version");
      sProjectTimestamp = p.get ("timestamp");
    }
    if (sProjectVersion == null)
    {
      sProjectVersion = "undefined";
      LOGGER.warn ("Failed to load version number");
    }
    BUILD_VERSION = sProjectVersion;
    if (sProjectTimestamp == null)
    {
      sProjectTimestamp = "undefined";
      LOGGER.warn ("Failed to load timestamp");
    }
    BUILD_TIMESTAMP = sProjectTimestamp;
  }

  private CDcngVersion ()
  {}
}
