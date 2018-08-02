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
package org.eclipse.che.api.testing.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** Result of {@link TestExecutionContext} request */
@DTO
public interface TestDetectionResult {

  /** @return true if requested document has tests, false otherwise */
  boolean isTestFile();

  void setTestFile(boolean testFile);

  /**
   * List of the test positions in document
   *
   * @return
   */
  List<TestPosition> getTestPosition();

  void setTestPosition(List<TestPosition> testPosition);
}
