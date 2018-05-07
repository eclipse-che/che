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
package org.eclipse.che.api.project.shared.dto;

import java.util.List;
import java.util.Map;
import org.eclipse.che.api.core.rest.shared.dto.Hyperlinks;
import org.eclipse.che.api.core.rest.shared.dto.Link;
import org.eclipse.che.api.workspace.shared.dto.ProjectConfigDto;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.dto.shared.DTO;

/** @author andrew00x */
@DTO
public interface ItemReference extends Hyperlinks {
  /** Get name of item. */
  String getName();

  /** Set name of item. */
  void setName(String name);

  ItemReference withName(String name);

  /** Get type of item, e.g. "file", "folder" or "project". */
  String getType();

  /** Set type of item, e.g. "file" or "folder" or "project". */
  void setType(String type);

  ItemReference withType(String type);

  /** Get project path. */
  String getProject();

  ItemReference withProject(String project);

  /** Get path of item. */
  String getPath();

  /** Set path of item. */
  void setPath(String path);

  ItemReference withPath(String path);

  @Override
  ItemReference withLinks(List<Link> links);

  /** Attributes */
  Map<String, String> getAttributes();

  void setAttributes(Map<String, String> attributes);

  ItemReference withAttributes(Map<String, String> attributes);

  /** last modified date. */
  long getModified();

  void setModified(long modified);

  ItemReference withModified(long modified);

  /** content length for file */
  long getContentLength();

  void setContentLength(long length);

  ItemReference withContentLength(long length);

  /**
   * The method can return {@code null} value. {@link ProjectConfigDto} exist only for project and
   * modules in other cases it is null.
   */
  @Nullable
  ProjectConfigDto getProjectConfig();

  void setProjectConfig(ProjectConfigDto config);
}
