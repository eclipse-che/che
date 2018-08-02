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
package org.eclipse.che.api.project.shared.dto;

import org.eclipse.che.api.project.shared.ImportProgressRecord;
import org.eclipse.che.dto.shared.DTO;

/**
 * DTO of {@link ImportProgressRecord}.
 *
 * @author Vlad Zhukovskyi
 * @since 5.9.0
 */
@DTO
public interface ImportProgressRecordDto extends ImportProgressRecord {
  void setNum(int num);

  ImportProgressRecordDto withNum(int num);

  void setLine(String line);

  ImportProgressRecordDto withLine(String line);

  void setProjectName(String projectName);

  ImportProgressRecordDto withProjectName(String projectName);
}
