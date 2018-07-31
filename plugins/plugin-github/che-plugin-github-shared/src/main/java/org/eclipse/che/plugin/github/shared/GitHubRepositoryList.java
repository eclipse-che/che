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
package org.eclipse.che.plugin.github.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * List of GitHub repositories for paging view.
 *
 * @author <a href="mailto:ashumilova@codenvy.com">Ann Shumilova</a>
 * @version $Id:
 */
@DTO
public interface GitHubRepositoryList {

  /** @return {@link List} the list of repositories */
  List<GitHubRepository> getRepositories();

  void setRepositories(List<GitHubRepository> repositories);

  /**
   * Link to the first page of the repositories list, if paging is used.
   *
   * @return {@link String} first page link
   */
  String getFirstPage();

  void setFirstPage(String page);

  /**
   * Link to the previous page of the repositories list, if paging is used.
   *
   * @return {@link String} previous page link
   */
  String getPrevPage();

  void setPrevPage(String page);

  /**
   * Link to the next page of the repositories list, if paging is used.
   *
   * @return {@link String} next page link
   */
  String getNextPage();

  void setNextPage(String page);

  /**
   * Link to the last page of the repositories list, if paging is used.
   *
   * @return {@link String} last page's link
   */
  String getLastPage();

  void setLastPage(String page);
}
