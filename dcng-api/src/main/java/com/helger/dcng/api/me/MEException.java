package com.helger.dcng.api.me;

import javax.annotation.Nullable;

/**
 * Base exception for all Message Exchange exceptions.
 *
 * @author Philip Helger
 */
public abstract class MEException extends Exception
{
  public MEException (@Nullable final String sMsg)
  {
    super (sMsg);
  }

  public MEException (@Nullable final String sMsg, @Nullable final Throwable aCause)
  {
    super (sMsg, aCause);
  }
}
