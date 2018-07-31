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
package org.eclipse.che.ide.api.resources;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.che.api.project.shared.Constants;
import org.eclipse.che.api.project.shared.SearchOccurrence;
import org.eclipse.che.api.project.shared.dto.SearchOccurrenceDto;
import org.eclipse.che.api.project.shared.dto.SearchResultDto;

/** @author Vitalii Parfonov */
public class SearchItemReference {

  private String name;
  private String path;
  private String project;
  private String contentUrl;
  private List<SearchOccurrence> occurrences;

  public SearchItemReference(SearchResultDto searchResultDto) {
    name = searchResultDto.getItemReference().getName();
    path = searchResultDto.getItemReference().getPath();
    project = searchResultDto.getItemReference().getProject();
    if (searchResultDto.getItemReference().getLink(Constants.LINK_REL_GET_CONTENT) != null) {
      contentUrl =
          searchResultDto.getItemReference().getLink(Constants.LINK_REL_GET_CONTENT).getHref();
    }
    final List<SearchOccurrenceDto> dtos = searchResultDto.getSearchOccurrences();
    occurrences = new ArrayList<>(dtos.size());
    for (SearchOccurrence dto : dtos) {
      occurrences.add(new SearchOccurrenceImpl(dto));
    }
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPath() {
    return path;
  }

  public void setPath(String path) {
    this.path = path;
  }

  public String getProject() {
    return project;
  }

  public void setProject(String project) {
    this.project = project;
  }

  public List<SearchOccurrence> getOccurrences() {
    return occurrences;
  }

  public void setOccurrences(List<SearchOccurrence> occurrences) {
    this.occurrences = occurrences;
  }

  public String getContentUrl() {
    return contentUrl;
  }

  public void setContentUrl(String contentUrl) {
    this.contentUrl = contentUrl;
  }
}
