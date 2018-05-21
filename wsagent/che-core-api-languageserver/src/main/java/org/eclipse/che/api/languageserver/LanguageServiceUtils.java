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
package org.eclipse.che.api.languageserver;

/**
 * Language service service utilities
 *
 * @author Thomas Mäder
 * @author Vitalii Parfonov
 * @author Yevhen Vydolob
 * @author Dmytro Kulieshov
 */
public class LanguageServiceUtils {

  private static final String PROJECTS = "/projects";
  private static final String FILE_PROJECTS = "file:///projects";

  public static String prefixURI(String relativePath) {
    return FILE_PROJECTS + relativePath;
  }

  public static String removePrefixUri(String uri) {
    return uri.startsWith(FILE_PROJECTS) ? uri.substring(FILE_PROJECTS.length()) : uri;
  }

  public static String removeUriScheme(String uri) {
    return uri.startsWith(FILE_PROJECTS) ? uri.substring("file://".length()) : uri;
  }

  public static boolean truish(Boolean b) {
    return b != null && b;
  }

  public static boolean isProjectUri(String path) {
    return path.startsWith(FILE_PROJECTS);
  }

  public static boolean isStartWithProject(String path) {
    return path.startsWith(PROJECTS);
  }

  public static String prefixProject(String path) {
    return path.startsWith(PROJECTS) ? path : PROJECTS + path;
  }
}
