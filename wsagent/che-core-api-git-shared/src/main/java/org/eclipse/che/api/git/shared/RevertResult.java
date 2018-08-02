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
package org.eclipse.che.api.git.shared;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;

/** @author Dmitrii Bocharov (bdshadow) */
@DTO
public interface RevertResult {

  public enum RevertStatus {
    FAILED("Failed to revert"),
    DIRTY_INDEX("The revert failed because of a dirty index"),
    DIRTY_WORKTREE("The revert failed because of a dirty working tree"),
    COULD_NOT_DELETE("The revert failed because a file could not be deleted");

    private final String value;

    private RevertStatus(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * @return a map containing paths to the conflicting files as keys and their {@link RevertStatus}
   *     as values
   */
  Map<String, RevertStatus> getConflicts();

  void setConflicts(Map<String, RevertStatus> conflicts);

  RevertResult withConflicts(Map<String, RevertStatus> conflicts);

  /** @return the successfully reverted list of files */
  List<String> getRevertedCommits();

  void setRevertedCommits(List<String> revertedCommits);

  RevertResult withRevertedCommits(List<String> revertedCommits);

  /** @return head after revert */
  String getNewHead();

  void setNewHead(String newHead);

  RevertResult withNewHead(String newHead);
}
