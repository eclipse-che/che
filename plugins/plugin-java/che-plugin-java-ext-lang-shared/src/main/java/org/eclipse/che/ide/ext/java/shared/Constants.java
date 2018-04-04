/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.ide.ext.java.shared;

/**
 * @author Artem Zatsarynnyi
 * @author Valeriy Svydenko
 */
public final class Constants {
  // project categories
  public static final String JAVA_CATEGORY = "Java";
  public static final String JAVA_ID = "java";
  // project attribute names
  public static final String LANGUAGE = "language";
  public static final String LANGUAGE_VERSION = "languageVersion";
  public static final String FRAMEWORK = "framework";
  public static final String CONTAINS_JAVA_FILES = "containsJavaFiles";
  public static final String SOURCE_FOLDER = "java.source.folder";
  public static final String OUTPUT_FOLDER = "java.output.folder";

  public static final String JAVAC = "javac";

  private Constants() {
    throw new UnsupportedOperationException("Unused constructor.");
  }
}
