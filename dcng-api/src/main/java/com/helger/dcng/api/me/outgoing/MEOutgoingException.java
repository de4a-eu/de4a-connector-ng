package com.helger.dcng.api.me.outgoing;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.helger.dcng.api.error.IDcngErrorCode;
import com.helger.dcng.api.me.MEException;

/**
 * Exception when sending messages via ME
 *
 * @author Philip Helger
 */
public class MEOutgoingException extends MEException
{
  private final IDcngErrorCode m_aErrorCode;

  protected MEOutgoingException (@Nullable final String sMsg, @Nullable final Throwable aCause, @Nullable final IDcngErrorCode aErrorCode)
  {
    super (sMsg, aCause);
    m_aErrorCode = aErrorCode;
  }

  public MEOutgoingException (@Nullable final String sMsg)
  {
    this (sMsg, null, null);
  }

  public MEOutgoingException (@Nullable final String sMsg, @Nullable final Throwable aCause)
  {
    this (sMsg, aCause, null);
  }

  public MEOutgoingException (@Nonnull final IDcngErrorCode aErrorCode, @Nullable final Throwable aCause)
  {
    this ("DCNG Error " + aErrorCode.getID (), aCause, aErrorCode);
  }

  public MEOutgoingException (@Nonnull final IDcngErrorCode aErrorCode, @Nullable final String sMsg)
  {
    this ("DCNG Error " + aErrorCode.getID () + " - " + sMsg, null, aErrorCode);
  }

  @Nullable
  public final IDcngErrorCode getErrorCode ()
  {
    return m_aErrorCode;
  }
}
