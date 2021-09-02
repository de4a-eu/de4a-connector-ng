/**
 * Copyright (C) 2021 Philip Helger (www.helger.com)
 * philip[at]helger[dot]com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.helger.rdc.core.phase4;

import java.io.File;
import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.helger.commons.datetime.PDTFactory;
import com.helger.commons.io.file.FilenameHelper;
import com.helger.commons.io.file.SimpleFileIO;
import com.helger.commons.string.StringHelper;
import com.helger.datetime.util.PDTIOHelper;
import com.helger.phase4.client.AS4ClientSentMessage;
import com.helger.phase4.client.IAS4RawResponseConsumer;
import com.helger.phase4.util.Phase4Exception;

public class RawResponseWriter implements IAS4RawResponseConsumer
{
  private static final Logger LOGGER = LoggerFactory.getLogger (RawResponseWriter.class);

  public void handleResponse (final AS4ClientSentMessage <byte []> aResponseEntity) throws Phase4Exception
  {
    final String sFolderName = Phase4Config.getSendResponseFolderName ();
    if (StringHelper.hasText (sFolderName))
    {
      final String sMessageID = aResponseEntity.getMessageID ();
      final LocalDateTime aLDT = PDTFactory.getCurrentLocalDateTime ();
      final String sFilename = StringHelper.getLeadingZero (aLDT.getYear (), 4) +
                               '/' +
                               StringHelper.getLeadingZero (aLDT.getMonthValue (), 2) +
                               '/' +
                               StringHelper.getLeadingZero (aLDT.getDayOfMonth (), 2) +
                               '/' +
                               PDTIOHelper.getTimeForFilename (aLDT.toLocalTime ()) +
                               '-' +
                               FilenameHelper.getAsSecureValidASCIIFilename (sMessageID) +
                               "-response.xml";
      final File aResponseFile = new File (sFolderName, sFilename);
      if (SimpleFileIO.writeFile (aResponseFile, aResponseEntity.getResponse ()).isSuccess ())
        LOGGER.info ("[phase4] Response file was written to '" + aResponseFile.getAbsolutePath () + "'");
      else
        LOGGER.error ("[phase4] Error writing response file to '" + aResponseFile.getAbsolutePath () + "'");
    }
    else
    {
      if (LOGGER.isDebugEnabled ())
        LOGGER.debug ("[phase4] As no response folder name is configured, no message will be stored");
    }
  }
}
