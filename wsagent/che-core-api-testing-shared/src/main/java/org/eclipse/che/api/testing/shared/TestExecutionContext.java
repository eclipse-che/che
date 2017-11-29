/*
 * Copyright (c) 2012-2017 Red Hat, Inc.
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
import java.util.Map;
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
   */
  List<String> getListOfTestClasses();

  void setListOfTestClasses(List<String> listOfTestClasses);

  /**
   * Returns map of additional parameters of this test context
   *
   * @return A map of additional parameters of this test context
   */
  Map<String, String> getTestContextParameters();

  void setTestContextParameters(Map<String, String> testContextParameters);

  /**
   * Returns a name of the test finder to be used for gathering all test classes
   *
   * @return A name of the test finder to be used for gathering all test classes
   */
  String getTestFinderName();

  void setTestFinderName(String testFinderName);

  enum ContextType {
    FILE,
    FOLDER,
    PROJECT,
    CURSOR_POSITION,
    SET
  }
}
