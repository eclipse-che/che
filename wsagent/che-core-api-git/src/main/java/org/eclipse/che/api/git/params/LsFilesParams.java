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

/**
 * Arguments holder for {@link org.eclipse.che.api.git.GitConnection#listFiles(LsFilesParams)} .
 *
 * @author Igor Vinokur
 */
public class LsFilesParams {

  private boolean isOthers;
  private boolean isModified;
  private boolean isStaged;
  private boolean isCached;
  private boolean isDeleted;
  private boolean isIgnored;
  private boolean isExcludeStandard;

  private LsFilesParams() {}

  /** Create new {@link LsFilesParams} instance with empty parameters. */
  public static LsFilesParams create() {
    return new LsFilesParams();
  }

  /** Show other (i.e. untracked) files in the output. */
  public boolean isOthers() {
    return isOthers;
  }

  public LsFilesParams withOthers(boolean others) {
    isOthers = others;
    return this;
  }

  /** Show modified files in the output. */
  public boolean isModified() {
    return isModified;
  }

  public LsFilesParams withModified(boolean modified) {
    isModified = modified;
    return this;
  }

  /** Show staged contents' object name, mode bits and stage number in the output. */
  public boolean isStaged() {
    return isStaged;
  }

  public LsFilesParams withStaged(boolean staged) {
    isStaged = staged;
    return this;
  }

  /** Show cached files in the output (default). */
  public boolean isCached() {
    return isCached;
  }

  public LsFilesParams withCached(boolean cached) {
    isCached = cached;
    return this;
  }

  /** Show deleted files in the output. */
  public boolean isDeleted() {
    return isDeleted;
  }

  public LsFilesParams withDeleted(boolean deleted) {
    isDeleted = deleted;
    return this;
  }

  /**
   * Show only ignored files in the output. When showing files in the index, print only those
   * matched by an exclude pattern. When showing "other" files, show only those matched by an
   * exclude pattern.
   */
  public boolean isIgnored() {
    return isIgnored;
  }

  public LsFilesParams withIgnored(boolean ignored) {
    isIgnored = ignored;
    return this;
  }

  /**
   * Add the standard Git exclusions: .git/info/exclude, .gitignore in each directory, and the
   * user’s global exclusion file.
   */
  public boolean isExcludeStandard() {
    return isExcludeStandard;
  }

  public LsFilesParams withExcludeStandard(boolean excludeStandard) {
    isExcludeStandard = excludeStandard;
    return this;
  }
}
