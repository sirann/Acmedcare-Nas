/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.acmedcare.nas.ftp.server.command.impl;

import com.acmedcare.nas.ftp.server.command.AbstractCommand;
import com.acmedcare.nas.ftp.server.command.impl.listing.FileFormater;
import com.acmedcare.nas.ftp.server.command.impl.listing.ListArgument;
import com.acmedcare.nas.ftp.server.command.impl.listing.ListArgumentParser;
import com.acmedcare.nas.ftp.server.command.impl.listing.MLSTFileFormater;
import com.acmedcare.nas.ftp.server.ftplet.FtpException;
import com.acmedcare.nas.ftp.server.ftplet.FtpFile;
import com.acmedcare.nas.ftp.server.ftplet.FtpReply;
import com.acmedcare.nas.ftp.server.ftplet.FtpRequest;
import com.acmedcare.nas.ftp.server.impl.FtpIoSession;
import com.acmedcare.nas.ftp.server.impl.FtpServerContext;
import com.acmedcare.nas.ftp.server.impl.LocalizedFtpReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * <strong>Internal class, do not use directly.</strong> <code>
 * MLST &lt;SP&gt; &lt;pathname&gt; &lt;CRLF&gt;</code><br>
 *
 * <p>Returns info on the file over the control connection.
 *
 * @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>
 */
public class MLST extends AbstractCommand {

  private final Logger LOG = LoggerFactory.getLogger(MLST.class);

  /** Execute command. */
  @Override
  public void execute(
      final FtpIoSession session, final FtpServerContext context, final FtpRequest request)
      throws IOException {

    // reset state variables
    session.resetState();

    // parse argument
    ListArgument parsedArg = ListArgumentParser.parse(request.getArgument());

    FtpFile file = null;
    try {
      file = session.getFileSystemView().getFile(parsedArg.getFile());
      if (file != null && file.doesExist()) {
        FileFormater formater = new MLSTFileFormater((String[]) session.getAttribute("MLST.types"));
        session.write(
            LocalizedFtpReply.translate(
                session,
                request,
                context,
                FtpReply.REPLY_250_REQUESTED_FILE_ACTION_OKAY,
                "MLST",
                formater.format(file)));
      } else {
        session.write(
            LocalizedFtpReply.translate(
                session,
                request,
                context,
                FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
                "MLST",
                null));
      }
    } catch (FtpException ex) {
      LOG.debug("Exception sending the file listing", ex);
      session.write(
          LocalizedFtpReply.translate(
              session,
              request,
              context,
              FtpReply.REPLY_501_SYNTAX_ERROR_IN_PARAMETERS_OR_ARGUMENTS,
              "MLST",
              null));
    }
  }
}
