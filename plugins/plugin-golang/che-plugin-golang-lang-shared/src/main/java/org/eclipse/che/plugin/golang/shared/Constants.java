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
package org.eclipse.che.plugin.golang.shared;

/** @author Eugene Ivantsov */
public final class Constants {
  /** Language attribute name */
  public static String LANGUAGE = "language";
  /** Node JS Project Type ID */
  public static String GOLANG_PROJECT_TYPE_ID = "golang";

  /** Default extension for Go files */
  public static String GOLANG_FILE_EXT = "go";

  private Constants() {
    throw new UnsupportedOperationException("You can't create instance of Constants class");
  }
}
