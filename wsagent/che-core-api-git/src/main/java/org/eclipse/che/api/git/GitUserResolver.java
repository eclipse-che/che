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
package org.eclipse.che.api.git;

import org.eclipse.che.api.git.shared.GitUser;

/**
 * Resolves {@link GitUser} for any git related operations.
 *
 * @author Max Shaposhnik
 */
public interface GitUserResolver {

  /**
   * Retrieves user for git operations.
   *
   * @return credentials of current user to execute git operation
   */
  GitUser getUser();
}
