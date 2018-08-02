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
package org.eclipse.che.api.core;

/**
 * Error codes that are used in exceptions. Defined error codes MUST BE in range <b>15000-32999</b>
 * inclusive.
 *
 * @author Igor Vinokur
 */
public class ErrorCodes {
  private ErrorCodes() {}

  public static final int NO_COMMITTER_NAME_OR_EMAIL_DEFINED = 15216;
  public static final int UNABLE_GET_PRIVATE_SSH_KEY = 32068;
  public static final int UNAUTHORIZED_GIT_OPERATION = 32080;
  public static final int UNAUTHORIZED_SVN_OPERATION = 32090;
  public static final int MERGE_CONFLICT = 32062;
  public static final int FAILED_CHECKOUT = 32063;
  public static final int FAILED_CHECKOUT_WITH_START_POINT = 32064;
  public static final int INIT_COMMIT_WAS_NOT_PERFORMED = 32082;

  public static final int NO_PROJECT_ON_FILE_SYSTEM = 10;
  public static final int NO_PROJECT_CONFIGURED_IN_WS = 11;
  public static final int PROJECT_TYPE_IS_NOT_REGISTERED = 12;
  public static final int ATTRIBUTE_NAME_PROBLEM = 13;
  public static final int NOT_UPDATED_PROJECT = 14;
}
