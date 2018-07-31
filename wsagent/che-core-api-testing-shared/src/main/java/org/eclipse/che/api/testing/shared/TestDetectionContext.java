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
package org.eclipse.che.api.testing.shared;

import org.eclipse.che.dto.shared.DTO;

/**
 * Request DTO for test framework and/or test method detection, result should be {@link
 * TestDetectionResult}
 */
@DTO
public interface TestDetectionContext {

  String getProjectPath();

  void setProjectPath(String projectPath);

  String getFilePath();

  void setFilePath(String filePath);

  int getOffset();

  void setOffset(int offset);
}
