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
package org.eclipse.che.ide.ext.java.shared.dto.search;

import java.util.List;
import java.util.Map;
import org.eclipse.che.dto.shared.DTO;
import org.eclipse.che.ide.ext.java.shared.dto.model.JavaProject;

/**
 * Response for FindUsages Request. Contains projects and matches.
 *
 * @author Evgen Vidolob
 */
@DTO
public interface FindUsagesResponse {
  /**
   * Gets projects.
   *
   * @return the projects
   */
  List<JavaProject> getProjects();

  /**
   * Sets projects.
   *
   * @param projects the projects
   */
  void setProjects(List<JavaProject> projects);

  /**
   * Matches mapped to java element handle.
   *
   * @return the map matches
   */
  Map<String, List<Match>> getMatches();

  /**
   * Sets matches.
   *
   * @param matches the matches
   */
  void setMatches(Map<String, List<Match>> matches);

  /**
   * Gets search element description.
   *
   * @return search element
   */
  String getSearchElementLabel();

  /**
   * Sets search element description.
   *
   * @param label the search element label
   */
  void setSearchElementLabel(String label);
}
