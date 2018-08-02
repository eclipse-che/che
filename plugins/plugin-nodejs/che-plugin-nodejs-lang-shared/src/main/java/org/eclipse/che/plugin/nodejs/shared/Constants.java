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
package org.eclipse.che.plugin.nodejs.shared;

/** @author Dmitry Shnurenko */
public final class Constants {
  /** Options for run command */
  public static final String RUN_PARAMETERS_ATTRIBUTE = "run.parameters";
  /** Language attribute name */
  public static String LANGUAGE = "language";
  /** Node JS Project Type ID */
  public static String NODE_JS_PROJECT_TYPE_ID = "node-js";

  /** Default extension for C files */
  public static String NODE_JS_FILE_EXT = "js";

  private Constants() {
    throw new UnsupportedOperationException("You can't create instance of Constants class");
  }
}
