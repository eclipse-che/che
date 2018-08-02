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
package org.eclipse.che.selenium.core.constant;

/** @author Anatolii Bazko */
public final class TestGitConstants {
  public static final String GIT_ADD_TO_INDEX_SUCCESS = "Git index updated";
  public static final String GIT_NOTHING_TO_ADD =
      "Selected item does not have any unstaged changes";
  public static final String GIT_REMOVE_FROM_INDEX_SUCCESS = "Files removed from index";
  public static final String GIT_INITIALIZED_SUCCESS = "Repository initialized";
  public static final String GIT_REPO_DELETE = "Git repository deleted";
  public static final String COMMIT_MESSAGE_SUCCESS = "Committed with revision";
  public static final String CONFIGURING_PROJECT_AND_CLONING_SOURCE_CODE =
      "Configuring project and cloning source code.";
}
