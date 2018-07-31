/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which is available at http://www.eclipse.org/legal/epl-2.0.html
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.api.git.shared;

/** @author Anatoliy Bazko */
public class Constants {
  public static final int DEFAULT_PAGE_SIZE = 20;
  public static final String DEFAULT_PAGE_SIZE_QUERY_PARAM = "20";
  public static final String EVENT_GIT_FILE_CHANGED = "event/git-change";
  public static final String COMMIT_IN_PROGRESS_ERROR = "Commit in progress";
  public static final String CHECKOUT_IN_PROGRESS_ERROR = "Checkout in progress";
  public static final String NOT_A_GIT_REPOSITORY_ERROR = "Not a git repository";

  private Constants() {}
}
