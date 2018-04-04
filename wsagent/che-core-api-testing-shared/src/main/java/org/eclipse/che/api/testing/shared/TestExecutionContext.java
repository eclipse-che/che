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
package org.eclipse.che.api.testing.shared;

import java.util.List;
import org.eclipse.che.dto.shared.DTO;

/** Context which provides information about test execution. */
@DTO
public interface TestExecutionContext {

  /** returns name of the test framework. */
  String getFrameworkName();

  void setFrameworkName(String name);

  /** returns path to the project. */
  String getProjectPath();

  void setProjectPath(String projectPath);

  /** returns path to the file. */
  String getFilePath();

  void setFilePath(String filePath);

  /** returns type of the test. */
  ContextType getContextType();

  void setContextType(ContextType contextType);

  /** returns cursor position. */
  int getCursorOffset();

  void setCursorOffset(int offset);

  void setDebugModeEnable(Boolean enable);

  /** returns state of the debug mode */
  Boolean isDebugModeEnable();

  TestExecutionContext withDebugModeEnable(Boolean enable);

  /**
   * returns a list with paths of the test files relative to the project. The list should be
   * initialized when value of {@link ContextType} is {@link ContextType.SET}
   *
   * @param listOfTestClasses
   */
  void setListOfTestClasses(List<String> listOfTestClasses);

  List<String> getListOfTestClasses();

  enum ContextType {
    FILE,
    FOLDER,
    PROJECT,
    CURSOR_POSITION,
    SET
  }
}
