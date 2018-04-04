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
package org.eclipse.che.ide.ext.git.client.compare;

import org.eclipse.che.ide.api.resources.Project;
import org.eclipse.che.ide.ext.git.client.compare.FileStatus.Status;

/**
 * Describes changed in any way project files. Supports adding and removing items dynamically.
 *
 * @author Mykola Morhun
 */
public class MutableAlteredFiles extends AlteredFiles {

  /**
   * Parses raw git diff string and creates advanced representation.
   *
   * @param project the project under diff operation
   * @param diff plain result of git diff operation
   */
  public MutableAlteredFiles(Project project, String diff) {
    super(project, diff);
  }

  /**
   * Creates mutable altered files list based on changes from another project.
   *
   * @param project the project under diff operation
   * @param alteredFiles changes from another project
   */
  public MutableAlteredFiles(Project project, AlteredFiles alteredFiles) {
    super(project, "");
    this.alteredFilesStatuses.putAll(alteredFiles.alteredFilesStatuses);
    this.alteredFilesList.addAll(alteredFiles.alteredFilesList);
  }

  /**
   * Creates an empty list of altered files.
   *
   * @param project the project under diff operation
   */
  public MutableAlteredFiles(Project project) {
    super(project, "");
  }

  /**
   * Adds or updates a file in altered file list. If given file is already exists does nothing.
   *
   * @param file full path to file and its name relatively to project root
   * @param status git status of the file
   * @return true if file was added or updated and false if the file is already exists in this list
   */
  public boolean addFile(String file, Status status) {
    if (status.equals(alteredFilesStatuses.get(file))) {
      return false;
    }

    if (alteredFilesStatuses.put(file, status) == null) {
      // it's not a status change, new file was added
      alteredFilesList.add(file);
    }
    return true;
  }

  /**
   * Removes given file from the altered files list. If given file isn't present does nothing.
   *
   * @param file full path to file and its name relatively to project root
   * @return true if the file was deleted and false otherwise
   */
  public boolean removeFile(String file) {
    alteredFilesStatuses.remove(file);
    return alteredFilesList.remove(file);
  }
}
