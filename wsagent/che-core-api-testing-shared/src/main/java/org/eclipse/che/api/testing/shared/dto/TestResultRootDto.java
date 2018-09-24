/*
 * Copyright (c) 2016 Rogue Wave Software, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Rogue Wave Software, Inc. - initial API and implementation
 */
package org.eclipse.che.api.testing.shared.dto;

import java.util.List;
import org.eclipse.che.api.testing.shared.common.TestResultStatus;
import org.eclipse.che.dto.shared.DTO;

/**
 * Test result root DTO.
 *
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface TestResultRootDto {

  /**
   * Returns related test framework name.
   *
   * @return related test framework name
   */
  String getTestFrameworkName();

  /**
   * Sets related test framework name.
   *
   * @param frameworkName
   */
  void setTestFrameworkName(String frameworkName);

  /**
   * Returns overall test result status (success/failure, etc.).
   *
   * @return overall test result status
   */
  TestResultStatus getStatus();

  /**
   * Sets overall test result status
   *
   * @param status
   */
  void setStatus(TestResultStatus status);

  /**
   * Returns test result root name.
   *
   * @return test result root name
   */
  String getName();

  /**
   * Sets test result root name.
   *
   * @param label
   */
  void setName(String label);

  /**
   * Returns additional text (i.e. test execution time) that will be added as info text in related
   * tree element label.
   *
   * @return info text
   */
  String getInfoText();

  /**
   * Sets additional info text.
   *
   * @param infoText
   */
  void setInfoText(String infoText);

  /**
   * Returns test result related stack trace DTO.
   *
   * @return test result related stack trace DTO
   */
  TestResultTraceDto getTrace();

  /**
   * Sets test result related stack trace DTO.
   *
   * @param traceDto
   */
  void setTrace(TestResultTraceDto traceDto);

  /**
   * Returns path to test result root.
   *
   * @return path to test result root
   */
  List<String> getResultPath();

  /**
   * Sets path to test result root.
   *
   * @param resultPath
   */
  void setResultPath(List<String> resultPath);

  /**
   * Returns <code>true</code> if this result root is empty (has no test results at all).
   *
   * @return <code>true</code> if this result root is empty, <code>false</code> otherwise
   */
  boolean isEmpty();

  /**
   * Sets whether this result root is empty (has no test results at all)
   *
   * @param isEmpty
   */
  void setEmpty(boolean isEmpty);
}
