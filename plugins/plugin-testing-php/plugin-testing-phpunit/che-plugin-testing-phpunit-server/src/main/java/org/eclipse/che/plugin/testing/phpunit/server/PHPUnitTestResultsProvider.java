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
package org.eclipse.che.plugin.testing.phpunit.server;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.eclipse.che.api.testing.shared.common.TestResultStatus;
import org.eclipse.che.api.testing.shared.common.TestResultType;
import org.eclipse.che.api.testing.shared.dto.SimpleLocationDto;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceDto;
import org.eclipse.che.api.testing.shared.dto.TestResultTraceFrameDto;
import org.eclipse.che.dto.server.DtoFactory;
import org.eclipse.che.plugin.testing.phpunit.server.model.AbstractPHPUnitTestResult;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestCase;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestException;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestRoot;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestSuite;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTraceFrame;

/**
 * Test results provider.
 *
 * @author Bartlomiej Laczkowski
 */
class PHPUnitTestResultsProvider {

  private final Map<String, AbstractPHPUnitTestResult> testResultsCache = new HashMap<>();

  /**
   * Builds and returns test results root.
   *
   * @param resultsRoot
   * @return test results root
   */
  public TestResultRootDto getTestResultsRoot(PHPUnitTestRoot resultsRoot) {
    TestResultRootDto testResultRootDto =
        DtoFactory.getInstance().createDto(TestResultRootDto.class);
    testResultRootDto.setTestFrameworkName(PHPUnitTestRunner.RUNNER_ID);
    testResultRootDto.setStatus(getStatus(resultsRoot.getStatus()));
    testResultRootDto.setResultPath(Collections.singletonList("php-tests-root"));
    testResultRootDto.setName(getRootLabel(resultsRoot.getStatus()));
    testResultRootDto.setInfoText(getTimeString(resultsRoot.getTime()));
    testResultRootDto.setEmpty(resultsRoot.getChildren() == null);
    // Add PHP related test result to cache
    testResultsCache.put(getKey(testResultRootDto.getResultPath()), resultsRoot);
    return testResultRootDto;
  }

  /**
   * Builds and returns test results for given path.
   *
   * @param testResultsPath
   * @return test results for given path
   */
  public List<TestResultDto> getTestResults(List<String> testResultsPath) {
    String key = getKey(testResultsPath);
    AbstractPHPUnitTestResult phpTestResult = testResultsCache.get(key);
    int testChildCounter = 0;
    List<TestResultDto> testResults = new ArrayList<>();
    for (AbstractPHPUnitTestResult phpChildResult : phpTestResult.getChildren()) {
      List<String> childResultsPath = new ArrayList<>(testResultsPath);
      childResultsPath.add(String.valueOf(testChildCounter++));
      TestResultDto testResultDto = getTestResult(phpChildResult, childResultsPath);
      testResults.add(testResultDto);
    }
    return testResults;
  }

  private TestResultDto getTestResult(
      AbstractPHPUnitTestResult phpTestResult, List<String> testResultPath) {
    TestResultDto testResultDto = DtoFactory.getInstance().createDto(TestResultDto.class);
    testResultDto.setStatus(getStatus(phpTestResult.getStatus()));
    testResultDto.setResultPath(testResultPath);
    testResultDto.setName(phpTestResult.getName());
    testResultDto.setTrace(getTestTrace(phpTestResult));
    testResultDto.setInfoText(getTimeString(phpTestResult.getTime()));
    testResultDto.setType(
        phpTestResult instanceof PHPUnitTestSuite
            ? TestResultType.TEST_SUITE
            : TestResultType.TEST_CASE);
    SimpleLocationDto simpleLocationDto =
        DtoFactory.getInstance().createDto(SimpleLocationDto.class);
    simpleLocationDto.setResourcePath(phpTestResult.getFile());
    simpleLocationDto.setLineNumber(phpTestResult.getLine() - 1);
    testResultDto.setTestLocation(simpleLocationDto);
    // Add PHP related test result to cache
    testResultsCache.put(getKey(testResultDto.getResultPath()), phpTestResult);
    return testResultDto;
  }

  private TestResultTraceDto getTestTrace(AbstractPHPUnitTestResult phpTestResult) {
    TestResultTraceDto testResultTraceDto =
        DtoFactory.getInstance().createDto(TestResultTraceDto.class);
    if (phpTestResult instanceof PHPUnitTestCase) {
      PHPUnitTestCase phpTestCase = (PHPUnitTestCase) phpTestResult;
      PHPUnitTestException phpTestEvent = phpTestCase.getException();
      if (phpTestEvent != null) {
        testResultTraceDto.setMessage(
            phpTestEvent.getExceptionClass() + ": " + phpTestEvent.getMessage());
        List<TestResultTraceFrameDto> traceFrames = new ArrayList<>();
        for (PHPUnitTraceFrame phpTraceFrame : phpTestEvent.getTrace()) {
          TestResultTraceFrameDto testResultTraceFrameDto =
              DtoFactory.getInstance().createDto(TestResultTraceFrameDto.class);
          testResultTraceFrameDto.setTraceFrame(phpTraceFrame.toString());
          SimpleLocationDto simpleLocationDto =
              DtoFactory.getInstance().createDto(SimpleLocationDto.class);
          simpleLocationDto.setResourcePath(phpTraceFrame.getFile());
          simpleLocationDto.setLineNumber(phpTraceFrame.getLine() - 1);
          testResultTraceFrameDto.setLocation(simpleLocationDto);
          traceFrames.add(testResultTraceFrameDto);
        }
        testResultTraceDto.setTraceFrames(traceFrames);
        return testResultTraceDto;
      }
    }
    return null;
  }

  private String getKey(List<String> resultsPath) {
    StringBuilder sb = new StringBuilder();
    Iterator<String> resultsPathIterator = resultsPath.iterator();
    sb.append(resultsPathIterator.next());
    while (resultsPathIterator.hasNext()) {
      sb.append("->" + resultsPathIterator.next());
    }
    return sb.toString();
  }

  private TestResultStatus getStatus(int phpStatus) {
    switch (phpStatus) {
      case AbstractPHPUnitTestResult.STATUS_PASS:
        {
          return TestResultStatus.SUCCESS;
        }
      case AbstractPHPUnitTestResult.STATUS_WARNING:
        {
          return TestResultStatus.WARNING;
        }
      case AbstractPHPUnitTestResult.STATUS_FAIL:
        {
          return TestResultStatus.FAILURE;
        }
      case AbstractPHPUnitTestResult.STATUS_ERROR:
        {
          return TestResultStatus.ERROR;
        }
      case AbstractPHPUnitTestResult.STATUS_SKIP:
      case AbstractPHPUnitTestResult.STATUS_INCOMPLETE:
        {
          return TestResultStatus.SKIPPED;
        }
    }
    return null;
  }

  private String getRootLabel(int phpStatus) {
    switch (phpStatus) {
      case AbstractPHPUnitTestResult.STATUS_PASS:
        {
          return "Tests Passed";
        }
      case AbstractPHPUnitTestResult.STATUS_FAIL:
      case AbstractPHPUnitTestResult.STATUS_ERROR:
        {
          return "Tests Failed";
        }
      case AbstractPHPUnitTestResult.STATUS_SKIP:
      case AbstractPHPUnitTestResult.STATUS_INCOMPLETE:
        {
          return "Tests Skipped";
        }
    }
    return "Test Results";
  }

  private String getTimeString(double time) {
    return "(" + (new DecimalFormat("0.000")).format(time) + " ms)";
  }
}
