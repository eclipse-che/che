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
package org.eclipse.che.plugin.pullrequest.shared;

/**
 * Shared constants for the contribution project type.
 *
 * @author Kevin Pollet
 */
public final class ContributionProjectTypeConstants {
  public static final String CONTRIBUTION_PROJECT_TYPE_ID = "pullrequest";

  public static final String CONTRIBUTION_PROJECT_TYPE_DISPLAY_NAME = "contribution";

  /** Contribution mode variable used to name the local branch that is initialized. */
  public static final String CONTRIBUTE_LOCAL_BRANCH_NAME = "local_branch";

  /** Contribution mode variable used to know in which branch the contribution has to be pushed. */
  public static final String CONTRIBUTE_TO_BRANCH_VARIABLE_NAME = "contribute_to_branch";

  /** Disable instantiation. */
  private ContributionProjectTypeConstants() {}
}
