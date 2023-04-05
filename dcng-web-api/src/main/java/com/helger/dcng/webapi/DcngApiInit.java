package com.helger.dcng.webapi;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

import com.helger.dcng.webapi.as4.ApiPostLookupAndSendIt2;
import com.helger.dcng.webapi.as4.ApiPostSendIt2;
import com.helger.dcng.webapi.smp.ApiGetSmpDocTypes;
import com.helger.dcng.webapi.smp.ApiGetSmpEndpoints;
import com.helger.photon.api.APIDescriptor;
import com.helger.photon.api.APIPath;
import com.helger.photon.api.IAPIRegistry;

/**
 * Register all APIs
 *
 * @author Philip Helger
 */
@Immutable
public final class DcngApiInit
{
  private DcngApiInit ()
  {}

  public static void initAPI (@Nonnull final IAPIRegistry aAPIRegistry)
  {
    // SMP stuff
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.get ("/smp/doctypes/{pid}"), ApiGetSmpDocTypes.class));
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.get ("/smp/endpoints/{pid}/{doctypeid}"),
                                                 ApiGetSmpEndpoints.class));

    // AS4 stuff
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.post ("/it2/send"), ApiPostSendIt2.class));
    aAPIRegistry.registerAPI (new APIDescriptor (APIPath.post ("/it2/lookup/send"), new ApiPostLookupAndSendIt2 ()));
  }
}
