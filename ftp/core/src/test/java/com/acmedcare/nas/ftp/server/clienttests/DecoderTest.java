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

package com.acmedcare.nas.ftp.server.clienttests;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.MalformedInputException;

/** @author <a href="mailto:iskp.me@gmail.com">Elve.Xu</a>* */
public class DecoderTest extends ClientTestTemplate {
  byte[] b;
  private String dump = "4C 49 53 54 20 61 62 63 64 AE 2E 0D 0A".replace(" ", "");

  public DecoderTest() throws DecoderException {
    b = Hex.decodeHex(dump.toCharArray());
  }

  public void testDecodeError() throws CharacterCodingException {
    CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
    ByteBuffer buffer = ByteBuffer.wrap(b);

    try {
      decoder.decode(buffer);
      fail("Must throw MalformedInputException");
    } catch (MalformedInputException e) {
      // OK
    }
  }

  @Override
  protected FTPClient createFTPClient() throws Exception {
    FTPClient client = new MyFTPClient();
    client.setDefaultTimeout(10000);
    return client;
  }

  public void testInvalidCharacter() throws Exception {
    client.login(ADMIN_USERNAME, ADMIN_PASSWORD);

    ((MyFTPClient) client).sendRawCommand(b);
    client.completePendingCommand();

    assertEquals(501, client.getReplyCode());
  }

  private static class MyFTPClient extends FTPClient {
    public void sendRawCommand(byte[] b) throws IOException {
      OutputStream out = _socket_.getOutputStream();
      out.write(b);
    }
  }
}
