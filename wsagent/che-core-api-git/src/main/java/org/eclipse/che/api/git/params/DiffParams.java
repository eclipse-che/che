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
package org.eclipse.che.api.git.params;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.shared.DiffType;

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#diff(DiffParams)}.
 *
 * @author Igor Vinokur
 */
public class DiffParams {

  private List<String> fileFilter;
  private DiffType type;
  private String commitA;
  private String commitB;
  private int renameLimit;
  private boolean noRenames;
  private boolean isCached;

  private DiffParams() {}

  /** Create new {@link DiffParams} instance with empty parameters. */
  public static DiffParams create() {
    return new DiffParams();
  }

  /**
   * Returns filter of file to show diff. It may be either list of file names or name of directory
   * to show all files under them.
   */
  public List<String> getFileFilter() {
    return fileFilter == null ? new ArrayList<>() : fileFilter;
  }

  public DiffParams withFileFilter(List<String> fileFilter) {
    this.fileFilter = fileFilter;
    return this;
  }

  /** Returns type of diff output. */
  public DiffType getType() {
    return type;
  }

  public DiffParams withType(DiffType type) {
    this.type = type;
    return this;
  }

  /** Returns <code>true</code> if renames must not be showing in diff result. */
  public boolean isNoRenames() {
    return noRenames;
  }

  public DiffParams withNoRenames(boolean noRenames) {
    this.noRenames = noRenames;
    return this;
  }

  /**
   * Returns limit of showing renames in diff output. This attribute has sense if {@link #noRenames}
   * is <code>false</code>.
   */
  public int getRenameLimit() {
    return renameLimit;
  }

  public DiffParams withRenameLimit(int renameLimit) {
    this.renameLimit = renameLimit;
    return this;
  }

  /** Returns first commit to view changes. */
  public String getCommitA() {
    return commitA;
  }

  public DiffParams withCommitA(String commitA) {
    this.commitA = commitA;
    return this;
  }

  /** Returns second commit to view changes. */
  public String getCommitB() {
    return commitB;
  }

  public DiffParams withCommitB(String commitB) {
    this.commitB = commitB;
    return this;
  }

  /**
   * Returns <code>false</code> (default) view changes between {@link #commitA} and working tree
   * otherwise between {@link #commitA} and index.
   */
  public boolean isCached() {
    return isCached;
  }

  public DiffParams withCached(boolean cached) {
    isCached = cached;
    return this;
  }
}
