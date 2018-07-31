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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.dto.shared.DTO;

@DTO
public interface ProjectSearchRequestDto {
  String getPath();

  ProjectSearchRequestDto withPath(String path);

  String getName();

  ProjectSearchRequestDto withName(String name);

  String getText();

  ProjectSearchRequestDto withText(String text);

  int getMaxItems();

  ProjectSearchRequestDto withMaxItems(int maxItems);

  int getSkipCount();

  ProjectSearchRequestDto withSkipCount(int skipCount);
}
