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
package org.eclipse.che.plugin.optimized.testing.server;

import static org.eclipse.che.plugin.optimized.testing.server.FailedTestsStorageTest.EXPECTED_PATH_TO_FAILED_TESTS_FILE;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Collections;
import org.arquillian.smart.testing.TestSelection;
import org.arquillian.smart.testing.strategies.failed.FailedTestsDetector;
import org.assertj.core.api.Assertions;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;
import org.junit.After;
import org.junit.Test;

public class CheTestResultParserTest {

  @After
  public void removeFailedTestsFile() throws IOException {
    Files.deleteIfExists(EXPECTED_PATH_TO_FAILED_TESTS_FILE);
  }

  @Test
  public void should_load_failed_tests_from_file() {
    // given
    new FailedTestsStorageTest().prepareDtoAndStoreFirstFailingTests();
    FailedTestsDetector failedTestsDetector =
        new FailedTestsDetector(Paths.get(".", "target").toAbsolutePath().toFile());

    // when
    Collection<TestSelection> failedTests = failedTestsDetector.getTests();

    // then
    TestSelection firstTest = new TestSelection("my.cool.Test", failedTestsDetector.getName());
    TestSelection secondTest =
        new TestSelection("my.cool.second.Test", failedTestsDetector.getName());
    Assertions.assertThat(failedTests).containsExactly(firstTest, secondTest);
  }

  @Test
  public void should_not_load_any_failed_test_as_file_is_empty() {
    // given
    FailedTestsToStoreDto failedTestsToStore =
        new FailedTestsStorageTest().prepareDtoAndStoreFirstFailingTests();
    when(failedTestsToStore.getFailedTests()).thenReturn(Collections.emptyList());
    TestFailureResultStorage.saveFailedTests(failedTestsToStore);
    FailedTestsDetector failedTestsDetector =
        new FailedTestsDetector(Paths.get(".", "target").toAbsolutePath().toFile());

    // when
    Collection<TestSelection> failedTests = failedTestsDetector.getTests();

    // then
    Assertions.assertThat(failedTests).isEmpty();
  }

  @Test
  public void should_not_load_any_failed_test_as_file_does_not_exist() {
    // given
    FailedTestsDetector failedTestsDetector =
        new FailedTestsDetector(Paths.get(".", "target").toAbsolutePath().toFile());

    // when
    Collection<TestSelection> failedTests = failedTestsDetector.getTests();

    // then
    Assertions.assertThat(failedTests).isEmpty();
  }
}
