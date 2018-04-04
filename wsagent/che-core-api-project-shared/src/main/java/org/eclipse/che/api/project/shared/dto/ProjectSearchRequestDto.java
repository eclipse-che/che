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
