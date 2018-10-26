/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.util;

import com.google.gwt.regexp.shared.RegExp;

/**
 * Utility methods for validating file/folder/project name.
 *
 * @author Evgen Vidolob
 * @author Artem Zatsarynnyi
 */
public class NameUtils {
  private static RegExp FILE_NAME = RegExp.compile("^((?![*:\\/\\\\\"?<>|\0]).)+$");
  private static RegExp FOLDER_NAME = FILE_NAME;
  private static RegExp PROJECT_NAME = RegExp.compile("^[A-Za-z0-9_\\-\\.]+$");
  private static RegExp COMMAND_NAME = RegExp.compile("^((?![*\\/\\\\\"?<>|\0]).)+$");

  private NameUtils() {}

  /**
   * Check file name.
   *
   * @param name the name
   * @return {@code true} if name is valid and {@code false} otherwise
   */
  public static boolean checkFileName(String name) {
    return FILE_NAME.test(name);
  }

  /**
   * Check folder name.
   *
   * @param name the name
   * @return {@code true} if name is valid and {@code false} otherwise
   */
  public static boolean checkFolderName(String name) {
    return FOLDER_NAME.test(name);
  }

  /**
   * Check project name.
   *
   * @param name the name
   * @return {@code true} if name is valid and {@code false} otherwise
   */
  public static boolean checkProjectName(String name) {
    return PROJECT_NAME.test(name);
  }

  /** Returns {@code true} if name is valid, {@code false} otherwise. */
  public static boolean isValidCommandName(String name) {
    return COMMAND_NAME.test(name);
  }

  public static String getFileExtension(String name) {
    final int lastDotPosition = name.lastIndexOf('.');
    // name has no extension
    if (lastDotPosition < 0) {
      return "";
    }
    return name.substring(lastDotPosition + 1);
  }
}
