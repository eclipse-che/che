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
package org.eclipse.che.ide.ext.git.client;

import org.eclipse.che.api.git.shared.Branch;

/** @author Sergii Leschenko */
public class BranchFilterByRemote {
  private final String refsForRemoteRepository;

  public BranchFilterByRemote(String remoteName) {
    this.refsForRemoteRepository = "refs/remotes/" + remoteName + "/";
  }

  /**
   * Checks that branch linked to remote.
   *
   * @param branch branch for checking
   * @return {@code true} if branch linked to remote and else returns {@code false}
   */
  public boolean isLinkedTo(Branch branch) {
    return branch.getName().startsWith(refsForRemoteRepository);
  }

  /**
   * Determines simple name of branch. Example difference between name and simple name: name of
   * branch equals "refs/remotes/origin/master" and simple name of branch equals "master"
   *
   * @return simple name of branch
   */
  public String getBranchNameWithoutRefs(Branch branch) {
    return branch
        .getName()
        .replaceFirst(refsForRemoteRepository, "")
        .replaceFirst("refs/heads/", "");
  }
}
