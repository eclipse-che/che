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
package org.eclipse.che.plugin.cpp.shared;

/** @author Vitalii Parfonov */
public final class Constants {

  public static final String BINARY_NAME_ATTRIBUTE = "binaryName";
  public static final String COMPILATION_OPTIONS_ATTRIBUTE = "compilationOptions";
  /** Language attribute name */
  public static String LANGUAGE = "language";
  /** C Project Type ID */
  public static String C_PROJECT_TYPE_ID = "c";
  /** C++ Project Type ID */
  public static String CPP_PROJECT_TYPE_ID = "cpp";
  /** C Language */
  public static String C_LANG = "c_lang";
  /** C++ Language */
  public static String CPP_LANG = "cpp_lang";
  /** Default extension for C files */
  public static String C_EXT = "c";
  /** Default extension for C Headers files */
  public static String H_EXT = "h";
  /** Default extension for C++ files */
  public static String CPP_EXT = "cpp";

  private Constants() {}
}
