/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *   SAP           - implementation
 */
package org.eclipse.che.git.impl.jgit;

import static java.lang.System.lineSeparator;
import static org.eclipse.che.api.git.ReferenceType.BRANCH;
import static org.eclipse.che.api.git.ReferenceType.COMMIT;
import static org.eclipse.jgit.lib.Constants.HEAD;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.git.InfoPage;
import org.eclipse.che.api.git.Reference;
import org.eclipse.che.api.git.exception.GitException;
import org.eclipse.che.api.git.shared.Status;
import org.eclipse.jgit.api.StatusCommand;
import org.eclipse.jgit.api.errors.GitAPIException;

/**
 * Jgit implementation of {@link Status}
 *
 * @author Igor Vinokur
 */
public class JGitStatusImpl implements Status, InfoPage {

  private final Reference reference;
  private String branchName;
  private String refName;
  private boolean clean;
  private List<String> added;
  private List<String> changed;
  private List<String> removed;
  private List<String> missing;
  private List<String> modified;
  private List<String> untracked;
  private List<String> untrackedFolders;
  private List<String> conflicting;
  private String repositoryState;

  /**
   * @param reference current reference
   * @param statusCommand Jgit status command
   * @throws GitException when any error occurs
   */
  JGitStatusImpl(Reference reference, StatusCommand statusCommand) throws GitException {
    this.reference = reference;
    this.refName = reference.getName();
    this.branchName = reference.getType() == BRANCH ? refName : HEAD;

    org.eclipse.jgit.api.Status gitStatus;
    try {
      gitStatus = statusCommand.call();
    } catch (GitAPIException exception) {
      throw new GitException(exception.getMessage(), exception);
    }

    clean = gitStatus.isClean();
    added = new ArrayList<>(gitStatus.getAdded());
    changed = new ArrayList<>(gitStatus.getChanged());
    removed = new ArrayList<>(gitStatus.getRemoved());
    missing = new ArrayList<>(gitStatus.getMissing());
    modified = new ArrayList<>(gitStatus.getModified());
    untracked = new ArrayList<>(gitStatus.getUntracked());
    untrackedFolders = new ArrayList<>(gitStatus.getUntrackedFolders());
    conflicting = new ArrayList<>(gitStatus.getConflicting());
  }

  @Override
  public void writeTo(OutputStream out) throws IOException {
    StringBuilder status = new StringBuilder();

    status
        .append(reference.getType() == BRANCH ? "On branch " : "HEAD detached at ")
        .append(
            reference.getType() == COMMIT
                ? reference.getName().substring(0, 8)
                : reference.getName())
        .append(lineSeparator());
    if (isClean()) {
      status.append(lineSeparator()).append("nothing to commit, working directory clean");
    } else {
      if (!added.isEmpty() || !changed.isEmpty() || !removed.isEmpty()) {
        status.append(lineSeparator()).append("Changes to be committed:").append(lineSeparator());
        added.forEach(file -> status.append(lineSeparator()).append("\tnew file:   ").append(file));
        changed.forEach(
            file -> status.append(lineSeparator()).append("\tmodified:   ").append(file));
        removed.forEach(
            file -> status.append(lineSeparator()).append("\tdeleted:    ").append(file));
        status.append(lineSeparator());
      }
      if (!untracked.isEmpty() || !modified.isEmpty() || !missing.isEmpty()) {
        status
            .append(lineSeparator())
            .append("Changes not staged for commit:")
            .append(lineSeparator());
        untracked.forEach(
            file -> status.append(lineSeparator()).append("\tnew file:   ").append(file));
        modified.forEach(
            file -> status.append(lineSeparator()).append("\tmodified:   ").append(file));
        missing.forEach(
            file -> status.append(lineSeparator()).append("\tdeleted:    ").append(file));
        status.append(lineSeparator());
      }
      if (!conflicting.isEmpty()) {
        status.append(lineSeparator()).append("Unmerged paths:").append(lineSeparator());
        conflicting.forEach(
            file -> status.append(lineSeparator()).append("\tboth modified:   ").append(file));
      }
    }

    out.write(status.toString().getBytes());
  }

  @Override
  public boolean isClean() {
    return clean;
  }

  @Override
  public void setClean(boolean clean) {
    this.clean = clean;
  }

  @Override
  public String getRefName() {
    return refName;
  }

  @Override
  public void setRefName(String refName) {
    this.refName = refName;
  }

  @Override
  public String getBranchName() {
    return branchName;
  }

  @Override
  public void setBranchName(String branchName) {
    this.branchName = branchName;
  }

  @Override
  public List<String> getAdded() {
    return added;
  }

  @Override
  public void setAdded(List<String> added) {
    this.added = added;
  }

  @Override
  public List<String> getChanged() {
    return changed;
  }

  @Override
  public void setChanged(List<String> changed) {
    this.changed = changed;
  }

  @Override
  public List<String> getRemoved() {
    return removed;
  }

  @Override
  public void setRemoved(List<String> removed) {
    this.removed = removed;
  }

  @Override
  public List<String> getMissing() {
    return missing;
  }

  @Override
  public void setMissing(List<String> missing) {
    this.missing = missing;
  }

  @Override
  public List<String> getModified() {
    return modified;
  }

  @Override
  public void setModified(List<String> modified) {
    this.modified = modified;
  }

  @Override
  public List<String> getUntracked() {
    return untracked;
  }

  @Override
  public void setUntracked(List<String> untracked) {
    this.untracked = untracked;
  }

  @Override
  public List<String> getUntrackedFolders() {
    return untrackedFolders;
  }

  @Override
  public void setUntrackedFolders(List<String> untrackedFolders) {
    this.untrackedFolders = untrackedFolders;
  }

  @Override
  public List<String> getConflicting() {
    return conflicting;
  }

  @Override
  public void setConflicting(List<String> conflicting) {
    this.conflicting = conflicting;
  }

  @Override
  public String getRepositoryState() {
    return this.repositoryState;
  }

  @Override
  public void setRepositoryState(String repositoryState) {
    this.repositoryState = repositoryState;
  }
}
