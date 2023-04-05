package com.helger.dcng.api.me.incoming;

import javax.annotation.Nullable;

import com.helger.dcng.api.me.MEException;

/**
 * Exception when receiving messages via ME
 *
 * @author Philip Helger
 */
public class MEIncomingException extends MEException
{
  public MEIncomingException (@Nullable final String sMsg)
  {
    super (sMsg);
  }

  public MEIncomingException (@Nullable final String sMsg, @Nullable final Throwable aCause)
  {
    super (sMsg, aCause);
  }
}
