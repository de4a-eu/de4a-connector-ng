package com.helger.rdc.api.me.incoming;

import javax.annotation.Nonnull;

import eu.de4a.iem.xml.IVersatileWriter;

public interface ITODOWritableObject
{
  @Nonnull
  default IVersatileWriter <?> getWriter ()
  {
    // TODO
    return null;
  }

  public static class TodoRequest implements ITODOWritableObject
  {}

  public static class TodoResponse implements ITODOWritableObject
  {}
}
