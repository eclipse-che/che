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
package org.eclipse.che.plugin.github.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/**
 * List of GitHub pull requests for paging view.
 *
 * @author Stephane Tournie
 */
@DTO
public interface GitHubPullRequestList {

  /** @return {@link List} the list of repositories */
  List<GitHubPullRequest> getPullRequests();

  void setPullRequests(List<GitHubPullRequest> pullRequests);

  /**
   * Link to the first page of the pull requests list, if paging is used.
   *
   * @return {@link String} first page link
   */
  String getFirstPage();

  void setFirstPage(String page);

  /**
   * Link to the previous page of the pull requests list, if paging is used.
   *
   * @return {@link String} previous page link
   */
  String getPrevPage();

  void setPrevPage(String page);

  /**
   * Link to the next page of the pull requests list, if paging is used.
   *
   * @return {@link String} next page link
   */
  String getNextPage();

  void setNextPage(String page);

  /**
   * Link to the last page of the pull requests list, if paging is used.
   *
   * @return {@link String} last page's link
   */
  String getLastPage();

  void setLastPage(String page);
}
