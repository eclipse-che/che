/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.api.testing.shared.dto;

import java.util.List;
import org.eclipse.che.api.testing.shared.common.TestResultStatus;
import org.eclipse.che.api.testing.shared.common.TestResultType;
import org.eclipse.che.dto.shared.DTO;

/**
 * Test Result DTO.
 *
 * @author Bartlomiej Laczkowski
 */
@DTO
public interface TestResultDto {

  /**
   * Returns test result type (i.e. test case or test suite).
   *
   * @return test result type (i.e. test case or test suite)
   */
  TestResultType getType();

  /**
   * Sets test result type
   *
   * @param type
   */
  void setType(TestResultType type);

  /**
   * Returns test result status (success, failure, etc.)
   *
   * @return test result status
   */
  TestResultStatus getStatus();

  /**
   * Sets test result status
   *
   * @param status
   */
  void setStatus(TestResultStatus status);

  /**
   * Returns test result name (i.e. test suite/test case name)
   *
   * @return test result name
   */
  String getName();

  /**
   * Sets test result name
   *
   * @param name
   */
  void setName(String name);

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
   * Returns test result simple location DTO (i.e. file with related test case).
   *
   * @return test result simple location DTO
   */
  SimpleLocationDto getTestLocation();

  /**
   * Sets test result simple location DTO.
   *
   * @param simpleLocationDto
   */
  void setTestLocation(SimpleLocationDto simpleLocationDto);

  /**
   * Returns path to test result.
   *
   * @return path to test result
   */
  List<String> getResultPath();

  /**
   * Sets path to test result.
   *
   * @param resultPath
   */
  void setResultPath(List<String> resultPath);
}
