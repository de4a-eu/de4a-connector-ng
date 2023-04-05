package com.helger.dcng.jetty;

import java.io.IOException;

import com.helger.photon.jetty.JettyStopper;

public final class JettyStopDCNG
{
  public static void main (final String [] args) throws IOException
  {
    new JettyStopper ().setStopPort (9093).run ();
  }
}
