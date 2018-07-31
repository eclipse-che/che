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
package org.eclipse.che.ide.ext.git.client;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import org.eclipse.che.api.git.shared.Branch;

/** @author Sergii Leschenko */
public class BranchSearcher {

  /**
   * Get values of remote branches: filter remote branches due to selected remote repository.
   *
   * @param remoteName remote name for filtering
   * @param remoteBranches remote branches
   */
  @NotNull
  public List<String> getRemoteBranchesToDisplay(
      @NotNull String remoteName, @NotNull List<Branch> remoteBranches) {
    return getRemoteBranchesToDisplay(new BranchFilterByRemote(remoteName), remoteBranches);
  }

  /**
   * Get simple names of remote branches: filter remote branches due to selected remote repository.
   */
  @NotNull
  public List<String> getRemoteBranchesToDisplay(
      BranchFilterByRemote filterByRemote, @NotNull List<Branch> remoteBranches) {
    List<String> branches = new ArrayList<>();

    if (remoteBranches.isEmpty()) {
      branches.add("master");
      return branches;
    }

    for (int i = 0; i < remoteBranches.size(); i++) {
      Branch branch = remoteBranches.get(i);
      if (filterByRemote.isLinkedTo(branch)) {
        branches.add(filterByRemote.getBranchNameWithoutRefs(branch));
      }
    }

    if (branches.isEmpty()) {
      branches.add("master");
    }
    return branches;
  }

  /**
   * Get simple names of local branches.
   *
   * @param localBranches local branches
   */
  @NotNull
  public List<String> getLocalBranchesToDisplay(@NotNull List<Branch> localBranches) {
    List<String> branches = new ArrayList<>();

    if (localBranches.isEmpty()) {
      branches.add("master");
      return branches;
    }

    for (Branch branch : localBranches) {
      branches.add(branch.getDisplayName());
    }

    return branches;
  }
}
