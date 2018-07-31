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

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** @author Dmitriy Vyshinskiy */
@DTO
public interface Status {
  boolean isClean();

  void setClean(boolean isClean);

  /** @deprecated Use {@link #getRefName()} instead. */
  String getBranchName();

  /** @deprecated Use #setRefName(String) instead. */
  void setBranchName(String branchName);

  /** Returns reference name e.g. branch, tag or commit id */
  String getRefName();

  void setRefName(String refName);

  /** New files that are staged in index. */
  List<String> getAdded();

  void setAdded(List<String> added);

  /** New files that are not staged in index. */
  List<String> getUntracked();

  void setUntracked(List<String> untracked);

  /** Modified files that are staged in index. */
  List<String> getChanged();

  void setChanged(List<String> changed);

  /** Modified files that are not staged in index. */
  List<String> getModified();

  void setModified(List<String> modified);

  /** Deleted files that are staged in index. */
  List<String> getRemoved();

  void setRemoved(List<String> removed);

  /** Deleted files that are not staged in index. */
  List<String> getMissing();

  void setMissing(List<String> missing);

  /**
   * Folders that contain only untracked files.
   *
   * @see #getUntracked()
   */
  List<String> getUntrackedFolders();

  void setUntrackedFolders(List<String> untrackedFolders);

  /** Files that have conflicts. */
  List<String> getConflicting();

  void setConflicting(List<String> added);

  String getRepositoryState();

  void setRepositoryState(String repositoryState);
}
