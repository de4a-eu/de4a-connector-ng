package com.helger.dcng.webapi;

/**
 * Special exception to be invoked if some of the API parameters are invalid or
 * missing. This exception is converted to an HTTP 400 "Bad Request".
 *
 * @author Philip Helger
 */
public class ApiParamException extends RuntimeException
{
  public ApiParamException (final String sMsg)
  {
    super (sMsg);
  }
}
