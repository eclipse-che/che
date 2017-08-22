package org.eclipse.search.internal.core.text;

import java.text.MessageFormat;

public class Messages {
  public static String format(String message, Object object) {
    return MessageFormat.format(message, new Object[] {object});
  }

  public static String format(String message, Object[] objects) {
    return MessageFormat.format(message, objects);
  }

  private Messages() {}
}
