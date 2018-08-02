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
package org.eclipse.che.plugin.java.plain.shared;

/** @author Valeriy Svydenko */
public final class PlainJavaProjectConstants {
  public static String JAVAC_PROJECT_ID = "javac";
  public static String JAVAC_PROJECT_NAME = "Java";
  public static String DEFAULT_SOURCE_FOLDER_VALUE = "src";
  public static String DEFAULT_OUTPUT_FOLDER_VALUE = "bin";

  public static String LIBRARY_FOLDER = "java.library.folder";
  public static String DEFAULT_LIBRARY_FOLDER_VALUE = "lib";

  private PlainJavaProjectConstants() {
    throw new UnsupportedOperationException("Unused constructor");
  }
}
