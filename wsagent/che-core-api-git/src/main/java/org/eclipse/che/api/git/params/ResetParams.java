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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.ResetRequest;
import org.eclipse.che.api.git.shared.ResetRequest.ResetType;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#reset(ResetParams)}.
 *
 * @author Igor Vinokur
 */
public class ResetParams {

  private String commit;
  private ResetType type;
  private List<String> filePattern;

  private ResetParams() {}

  /**
   * Create new {@link ResetParams} instance.
   *
   * @param commit hash of commit to which current head should be reset
   * @param type type of reset
   */
  public static ResetParams create(String commit, ResetType type) {
    return new ResetParams().withCommit(commit).withType(type);
  }

  /** @see ResetRequest#getCommit() */
  public String getCommit() {
    return commit;
  }

  /** @see ResetRequest#withCommit(String) */
  public ResetParams withCommit(String commit) {
    this.commit = commit;
    return this;
  }

  /** @see ResetRequest#getType() */
  public ResetType getType() {
    return type;
  }

  public ResetParams withType(ResetType type) {
    this.type = type;
    return this;
  }

  /** @see ResetRequest#getFilePattern() */
  public List<String> getFilePattern() {
    return filePattern == null ? new ArrayList<>() : filePattern;
  }

  /** @see ResetRequest#withFilePattern(List) */
  public ResetParams withFilePattern(List<String> filePattern) {
    this.filePattern = filePattern;
    return this;
  }
}
