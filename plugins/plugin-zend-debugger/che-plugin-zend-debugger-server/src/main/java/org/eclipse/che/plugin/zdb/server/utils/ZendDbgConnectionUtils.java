/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.zdb.server.utils;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.zip.Adler32;
import org.eclipse.che.plugin.zdb.server.ZendDebugger;
import org.eclipse.che.plugin.zdb.server.connection.IDbgMessage;

/**
 * Connection utilities.
 *
 * @author Bartlomiej Laczkowski
 */
public class ZendDbgConnectionUtils {

  /**
   * Writes line to data output stream.
   *
   * @param out
   * @param line
   * @throws IOException
   */
  public static final void writeString(DataOutputStream out, String line) throws IOException {
    byte[] byteArray = line.getBytes(Charset.forName(IDbgMessage.ENCODING));
    out.writeInt(byteArray.length);
    out.write(byteArray);
  }

  /**
   * Writes string as bytes to data output stream.
   *
   * @param out
   * @param byteArray
   * @throws IOException
   */
  public static final void writeStringAsBytes(DataOutputStream out, byte[] byteArray)
      throws IOException {
    out.writeInt(byteArray.length);
    out.write(byteArray);
  }

  /**
   * Writes line to data output stream with the use of given encoding.
   *
   * @param out
   * @param line
   * @param encoding
   * @throws IOException
   */
  public static final void writeEncodedString(DataOutputStream out, String line, String encoding)
      throws IOException {
    byte[] byteArray = getBytesFromText(line, encoding);
    out.writeInt(byteArray.length);
    out.write(byteArray);
  }

  /**
   * Reads string from data input stream.
   *
   * @param in
   * @return
   * @throws IOException
   */
  public static final String readString(DataInputStream in) throws IOException {
    return new String(readStringAsBytes(in), Charset.forName(IDbgMessage.ENCODING));
  }

  /**
   * Reads string as bytes from data output stream.
   *
   * @param in
   * @return
   * @throws IOException
   */
  public static final byte[] readStringAsBytes(DataInputStream in) throws IOException {
    int size = in.readInt();
    byte[] byteArray = new byte[size];
    in.readFully(byteArray);
    return byteArray;
  }

  /**
   * Reads string from data input stream with the use of given encoding.
   *
   * @param in
   * @param encoding
   * @return
   * @throws IOException
   */
  public static final String readEncodedString(DataInputStream in, String encoding)
      throws IOException {
    byte[] byteArray = readStringAsBytes(in);
    String rv = getTextFromBytes(byteArray, encoding);
    return rv;
  }

  /**
   * Computes bytes from text with the use of given encoding.
   *
   * @param text
   * @param encoding
   * @return bytes
   */
  public static final byte[] getBytesFromText(String text, String encoding) {
    try {
      return text.getBytes(encoding);
    } catch (Exception e) {
    }
    return text.getBytes(Charset.forName(IDbgMessage.ENCODING));
  }

  /**
   * Computes text from bytes with the use of given encoding.
   *
   * @param bytes
   * @param encoding
   * @return
   */
  public static final String getTextFromBytes(byte[] bytes, String encoding) {
    try {
      return new String(bytes, encoding);
    } catch (Exception e) {
    }
    return new String(bytes, Charset.forName(IDbgMessage.ENCODING));
  }

  /**
   * Checks if remote content is equal to corresponding local one.
   *
   * @param sizeToCheck
   * @param checksumToCheck
   * @param content
   * @return <code>true</code> if is equal, <code>false</code> otherwise
   */
  public static final boolean isRemoteContentEqual(
      int sizeToCheck, int checksumToCheck, byte[] content) {
    int checksum;
    if (sizeToCheck == content.length) {
      checksum = getContentCheckSum(content);
      return (checksumToCheck == checksum);
    }
    // Checks if the difference is just in the line endings
    try {
      int linesCount = 0;
      byte r = 13;
      byte n = 10;
      for (byte element : content) {
        if (element == n) {
          linesCount++;
        }
      }
      if (sizeToCheck == content.length + linesCount) {
        byte converted[] = new byte[content.length + linesCount];
        int i = 0;
        // Convert line endings UNIX -> Win
        for (byte element : content) {
          if (element == n) {
            converted[i] = r;
            i++;
          }
          converted[i] = element;
          i++;
        }
        checksum = getContentCheckSum(converted);
        if (checksumToCheck == checksum) {
          return true;
        }
        // Convert line endings Win -> UNIX
        for (int j = 0; j < content.length; j++) {
          if (content[j] == n) {
            converted[j] = r;
          } else if (content[j] == r) {
            converted[j] = n;
          }
        }
        checksum = getContentCheckSum(converted);
        return (checksumToCheck == checksum);
      }
    } catch (Exception e) {
      ZendDebugger.LOG.error(e.getMessage(), e);
    }
    return false;
  }

  /**
   * Computes check-sum for Zend debugger content comparison.
   *
   * @param content
   * @return check-sum
   */
  public static final int getContentCheckSum(byte[] content) {
    Adler32 checksumCalculator = new Adler32();
    checksumCalculator.update(content);
    return (int) checksumCalculator.getValue();
  }
}
