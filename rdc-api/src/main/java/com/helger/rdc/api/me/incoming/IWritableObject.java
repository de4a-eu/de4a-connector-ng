package com.helger.rdc.api.me.incoming;

import javax.annotation.Nonnull;

import eu.de4a.iem.xml.IVersatileWriter;

public interface IWritableObject
{
  @Nonnull
  default IVersatileWriter <?> getWriter ()
  {
    // TODO
    return null;
  }
}
