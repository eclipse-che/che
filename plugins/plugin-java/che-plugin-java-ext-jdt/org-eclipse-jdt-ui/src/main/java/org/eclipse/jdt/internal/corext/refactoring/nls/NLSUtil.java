package org.eclipse.jdt.internal.corext.refactoring.nls;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/** @author Evgen Vidolob */
public class NLSUtil {

  // no instances
  private NLSUtil() {}

  /**
   * Reads a stream into a String and closes the stream.
   *
   * @param is the input stream
   * @param encoding the encoding
   * @return the contents, or <code>null</code> if an error occurred
   */
  public static String readString(InputStream is, String encoding) {
    if (is == null) return null;
    BufferedReader reader = null;
    try {
      StringBuffer buffer = new StringBuffer();
      char[] part = new char[2048];
      int read = 0;
      reader = new BufferedReader(new InputStreamReader(is, encoding));

      while ((read = reader.read(part)) != -1) buffer.append(part, 0, read);

      return buffer.toString();

    } catch (IOException ex) {
    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {
        }
      }
    }
    return null;
  }
}
