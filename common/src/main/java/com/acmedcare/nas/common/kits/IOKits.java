/*
 * Copyright 1999-2018 Acmedcare+ Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.acmedcare.nas.common.kits;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.commons.lang3.StringUtils;

/**
 * IO related tool methods
 *
 * @author Elve.Xu
 */
public class IOKits {

  public static String toString(InputStream input, String encoding) throws IOException {
    return (null == encoding)
        ? toString(new InputStreamReader(input, "UTF-8"))
        : toString(new InputStreamReader(input, encoding));
  }

  public static String toString(Reader reader) throws IOException {
    CharArrayWriter sw = new CharArrayWriter();
    copy(reader, sw);
    return sw.toString();
  }

  public static long copy(Reader input, Writer output) throws IOException {
    char[] buffer = new char[1 << 12];
    long count = 0;
    for (int n = 0; (n = input.read(buffer)) >= 0; ) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  public static long copy(InputStream input, OutputStream output) throws IOException {
    byte[] buffer = new byte[1024];
    int bytesRead;
    int totalBytes = 0;
    while ((bytesRead = input.read(buffer)) != -1) {
      output.write(buffer, 0, bytesRead);

      totalBytes += bytesRead;
    }

    return totalBytes;
  }

  public static List<String> readLines(Reader input) throws IOException {
    BufferedReader reader = toBufferedReader(input);
    List<String> list = new ArrayList<String>();
    String line = null;
    for (; ; ) {
      line = reader.readLine();
      if (null != line) {
        if (StringUtils.isNotEmpty(line)) {
          list.add(line.trim());
        }
      } else {
        break;
      }
    }
    return list;
  }

  private static BufferedReader toBufferedReader(Reader reader) {
    return reader instanceof BufferedReader ? (BufferedReader) reader : new BufferedReader(reader);
  }

  public static boolean delete(File fileOrDir) throws IOException {
    if (fileOrDir == null) {
      return false;
    }

    if (fileOrDir.isDirectory()) {
      cleanDirectory(fileOrDir);
    }

    return fileOrDir.delete();
  }

  public static void cleanDirectory(File directory) throws IOException {
    if (!directory.exists()) {
      String message = directory + " does not exist";
      throw new IllegalArgumentException(message);
    }

    if (!directory.isDirectory()) {
      String message = directory + " is not a directory";
      throw new IllegalArgumentException(message);
    }

    File[] files = directory.listFiles();
    if (files == null) {
      throw new IOException("Failed to list contents of " + directory);
    }

    IOException exception = null;
    for (File file : files) {
      try {
        delete(file);
      } catch (IOException ioe) {
        exception = ioe;
      }
    }

    if (null != exception) {
      throw exception;
    }
  }

  public static void writeStringToFile(File file, String data, String encoding) throws IOException {
    OutputStream os = null;
    try {
      os = new FileOutputStream(file);
      os.write(data.getBytes(encoding));
      os.flush();
    } finally {
      if (null != os) {
        os.close();
      }
    }
  }

  public static byte[] tryDecompress(InputStream raw) throws Exception {

    try {
      GZIPInputStream gis = new GZIPInputStream(raw);
      ByteArrayOutputStream out = new ByteArrayOutputStream();

      IOKits.copy(gis, out);

      return out.toByteArray();
    } catch (Exception e) {
      e.printStackTrace();
    }

    return null;
  }
}
