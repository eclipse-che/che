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
package org.eclipse.che.ide.ext.git.client.compare;

import static com.google.common.base.Strings.isNullOrEmpty;
import static org.eclipse.che.ide.ext.git.client.compare.FileStatus.defineStatus;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

/**
 * Describes changed in any way files in git comparison process.
 *
 * @author Mykola Morhun
 */
public class AlteredFiles {

  protected final Project project;
  protected final LinkedHashMap<String, Status> alteredFilesStatuses;
  protected final List<String> alteredFilesList;
  protected final String commitA;
  protected final String commitB;

  /**
   * Parses raw git diff string and creates advanced representation.
   *
   * @param project the project under diff operation
   * @param diff plain result of git diff operation
   */
  public AlteredFiles(Project project, String diff) {
    this(project, diff, null, null);
  }

  public AlteredFiles(Project project, String diff, String commitA, String commitB) {
    this.project = project;

    alteredFilesStatuses = new LinkedHashMap<>();
    if (!isNullOrEmpty(diff)) {
      for (String item : diff.split("\n")) {
        if (item.length() < 3 || item.charAt(1) != '\t') {
          throw new IllegalArgumentException("Invalid git diff format. Invalid record: " + item);
        }
        alteredFilesStatuses.put(
            item.substring(2, item.length()), defineStatus(item.substring(0, 1)));
      }
    }

    alteredFilesList = new ArrayList<>(alteredFilesStatuses.keySet());

    this.commitA = commitA;
    this.commitB = commitB;
  }

  /**
   * Returns the commit A from the current diff.
   *
   * @return commit A or {@code null} if such wasn't provided
   */
  public String getCommitA() {
    return commitA;
  }

  /**
   * Returns the commit B from the current diff.
   *
   * @return commit B or {@code null} if such wasn't provided
   */
  public String getCommitB() {
    return commitB;
  }

  /** Returns project in which git repository is located. */
  public Project getProject() {
    return project;
  }

  /** Returns number of files in the diff. */
  public int getFilesQuantity() {
    return alteredFilesList.size();
  }

  public boolean isEmpty() {
    return 0 == alteredFilesList.size();
  }

  /** Returns this diff in map representation: altered file to its git status. */
  public Map<String, Status> getChangedFilesMap() {
    return alteredFilesStatuses;
  }

  /** Returns list of altered files in this git diff. */
  public List<String> getAlteredFilesList() {
    return alteredFilesList;
  }

  public Status getStatusByFilePath(String relativePathToChangedFile) {
    return alteredFilesStatuses.get(relativePathToChangedFile);
  }

  public Status getStatusByIndex(int index) {
    return alteredFilesStatuses.get(alteredFilesList.get(index));
  }

  public String getFileByIndex(int index) {
    return alteredFilesList.get(index);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    AlteredFiles that = (AlteredFiles) o;
    return Objects.equals(project, that.project)
        && Objects.equals(alteredFilesStatuses, that.alteredFilesStatuses)
        && Objects.equals(alteredFilesList, that.alteredFilesList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(project, alteredFilesStatuses, alteredFilesList);
  }
}
