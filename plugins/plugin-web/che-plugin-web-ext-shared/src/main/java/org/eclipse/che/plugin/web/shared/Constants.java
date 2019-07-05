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
package org.eclipse.che.plugin.web.shared;

/** Shared constants for Web plugin */
public final class Constants {

  /** Language attribute name */
  public static final String LANGUAGE = "language";

  /** TS Project Type ID */
  public static String TS_PROJECT_TYPE_ID = "typescript";

  /** TS Language */
  public static String TS_LANG = "typescript";

  /** Default extension for TS files */
  public static String TS_EXT = "ts";

  /** TypeScript file mime type */
  public static final String TS_MIME_TYPE = "application/typescript";

  /** Vue Project Type ID */
  public static String VUE_PROJECT_TYPE_ID = "vue";

  /** Vue Language */
  public static String VUE_LANG = "vue";

  /** Default extension for Vue files */
  public static String VUE_EXT = "vue";

  /** TypeScript file mime type */
  public static final String VUE_MIME_TYPE = "text/x-vue";

  private Constants() {}
}
