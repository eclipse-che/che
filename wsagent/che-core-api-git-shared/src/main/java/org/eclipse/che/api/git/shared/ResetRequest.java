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
import org.eclipse.che.dto.shared.DTO;

/**
 * Request to reset current HEAD to the specified state.
 *
 * @author andrew00x
 */
@DTO
public interface ResetRequest {
  /** Type of reset operation. */
  public enum ResetType {
    /** Change the ref and the index, the workdir is not changed (default). */
    MIXED("--mixed"),
    /** Just change the ref, the index and workdir are not changed. */
    SOFT("--soft"),
    /** Change the ref, the index and the workdir. */
    HARD("--hard"),
    /**
     * Change the ref, the index and the workdir that are different between respective commit and
     * HEAD.
     */
    KEEP("--keep"),
    /**
     * Resets the index and updates the files in the working tree that are different between
     * respective commit and HEAD, but keeps those which are different between the index and working
     * tree
     */
    MERGE("--merge");

    private final String value;

    private ResetType(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /** @return commit to which current head should be reset */
  String getCommit();

  void setCommit(String commit);

  ResetRequest withCommit(String commit);

  /** @return type of reset. */
  ResetType getType();

  void setType(ResetType type);

  /** @return files to reset the index */
  List<String> getFilePattern();

  void setFilePattern(List<String> filePattern);

  ResetRequest withFilePattern(List<String> filePattern);
}
