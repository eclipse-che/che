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
 * Describe single commit.
 *
 * @author andrew00x
 */
@DTO
public interface Revision {

  /** Parameter which shows that this revision is a fake revision (i.e. TO for Exception). */
  boolean isFake();

  /** Represents whether this revision is a fake revision. */
  void setFake(boolean fake);

  /** Returns the branch name from witch this commit was performed. */
  String getBranch();

  /** Set branch name from witch this commit was performed */
  void setBranch(String branch);

  /**
   * Create a {@link Revision} object based on a given branch name.
   *
   * @param branch branch name
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withBranch(String branch);

  /** Returns the commit id. */
  String getId();

  /** Set commit id. */
  void setId(String id);

  /**
   * Create a {@link Revision} object based on a given commit id.
   *
   * @param id commit id
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withId(String id);

  /** Returns the commit message. */
  String getMessage();

  /** Set commit message. */
  void setMessage(String message);

  /**
   * Create a {@link Revision} object based on a given commit message.
   *
   * @param message commit message
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withMessage(String message);

  /** Returns the commit time. */
  long getCommitTime();

  /** Set commit time. */
  void setCommitTime(long time);

  /**
   * Create a {@link Revision} object based on a given commit time.
   *
   * @param time commit time
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withCommitTime(long time);

  /** Returns the GitUser object which represents the committer. */
  GitUser getCommitter();

  /** Set GitUser object which represents the committer. */
  void setCommitter(GitUser committer);

  /**
   * Create a {@link Revision} object based on a given GitUser object which represents the committer
   *
   * @param committer GitUser object which represents the committer
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withCommitter(GitUser committer);

  /** Returns the commit author. */
  GitUser getAuthor();

  /** Set GitUser object which represents the commit author. */
  void setAuthor(GitUser author);

  /**
   * Create a {@link Revision} object based on a given GitUser object which represents the commit
   * author
   *
   * @param author GitUser object which represents the commit author
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withAuthor(GitUser author);

  /** Returns the branches where this commit is present. */
  List<Branch> getBranches();

  /** Set branches where this commit is present. */
  void setBranches(List<Branch> branches);

  /**
   * Create a {@link Revision} object based on a given list of branches where this commit is present
   *
   * @param branches a list of branches where this commit is present
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withBranches(List<Branch> branches);

  /** Returns a list of DiffCommitFile objects, which describes the changes in the commit files. */
  List<DiffCommitFile> getDiffCommitFile();

  /** Set a list of DiffCommitFile objects, which describes the changes in the commit files. */
  void setDiffCommitFile(List<DiffCommitFile> diffCommitFiles);

  /**
   * Create a {@link Revision} object based on a given list of DiffCommitFile objects, which
   * describes the changes in the commit files
   *
   * @param diffCommitFiles a list of DiffCommitFile objects, which describes the changes in the
   *     commit files
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withDiffCommitFile(List<DiffCommitFile> diffCommitFiles);

  /** Returns the commit parents. */
  List<String> getCommitParent();

  /** Set the commit parents. */
  void setCommitParent(List<String> commitParents);

  /**
   * Create a {@link Revision} object based on a given list of commit parents
   *
   * @param commitParents a list of commit parents
   * @return a {@link Revision} object that contains information about revision
   */
  Revision withCommitParent(List<String> commitParents);
}
