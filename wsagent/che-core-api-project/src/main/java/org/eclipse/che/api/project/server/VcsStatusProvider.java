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
package org.eclipse.che.api.project.server;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.ServerException;

/**
 * Version control system status provider.
 *
 * @author Igor Vinokur
 */
public interface VcsStatusProvider {

  /** Returns name of the version control system. */
  String getVcsName();

  /**
   * Get vcs status of the given file.
   *
   * @param path path to the given file
   */
  VcsStatus getStatus(String path) throws ServerException;

  /**
   * Get vcs status map of the given list of files.
   *
   * @param project project path
   * @param paths list of file paths (without project item)
   * @return map with full file path as a key and it's status as a value
   */
  Map<String, VcsStatus> getStatus(String project, List<String> paths) throws ServerException;

  enum VcsStatus {
    ADDED,
    MODIFIED,
    NOT_MODIFIED,
    UNTRACKED
  }
}
