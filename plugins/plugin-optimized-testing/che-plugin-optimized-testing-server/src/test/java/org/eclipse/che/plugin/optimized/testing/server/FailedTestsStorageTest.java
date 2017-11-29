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

import static org.eclipse.che.plugin.optimized.testing.server.TestFailureResultStorage.FAILED_TESTS_FILE_NAME;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.assertj.core.api.Assertions;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;
import org.junit.After;
import org.junit.Test;

public class FailedTestsStorageTest {

  public static final Path EXPECTED_PATH_TO_FAILED_TESTS_FILE =
      new TemporaryInternalFiles()
          .createTestReportDirectoryAction(Paths.get(".", "target").toFile())
          .getPath()
          .resolve(FAILED_TESTS_FILE_NAME);

  @After
  public void removeFailedTestsFile() throws IOException {
    Files.deleteIfExists(EXPECTED_PATH_TO_FAILED_TESTS_FILE);
  }

  @Test
  public void should_store_failed_tests_to_file() {
    // given
    FailedTestsToStoreDto failedTestsToStore = mock(FailedTestsToStoreDto.class);
    when(failedTestsToStore.getFailedTests())
        .thenReturn(Arrays.asList("my.cool.Test", "my.cool.second.Test"));
    when(failedTestsToStore.getProjectDir()).thenReturn("target");

    // when
    TestFailureResultStorage.saveFailedTests(failedTestsToStore);

    // then
    Assertions.assertThat(EXPECTED_PATH_TO_FAILED_TESTS_FILE)
        .exists()
        .hasContent("my.cool.Test\nmy.cool.second.Test");
  }

  @Test
  public void
      should_store_failed_tests_to_file_and_then_rewrite_the_content_with_another_failing_test() {
    // given
    FailedTestsToStoreDto failedTestsToStore = prepareDtoAndStoreFirstFailingTests();
    when(failedTestsToStore.getFailedTests()).thenReturn(Arrays.asList("my.another.failing.Test"));

    // when
    TestFailureResultStorage.saveFailedTests(failedTestsToStore);

    // then

    Assertions.assertThat(EXPECTED_PATH_TO_FAILED_TESTS_FILE)
        .exists()
        .hasContent("my.another.failing.Test");
  }

  @Test
  public void
      should_store_failed_tests_to_file_and_then_rewrite_the_content_with_none_failing_test() {
    // given
    FailedTestsToStoreDto failedTestsToStore = prepareDtoAndStoreFirstFailingTests();
    when(failedTestsToStore.getFailedTests()).thenReturn(Collections.emptyList());

    // when
    TestFailureResultStorage.saveFailedTests(failedTestsToStore);

    // then
    Assertions.assertThat(EXPECTED_PATH_TO_FAILED_TESTS_FILE).exists().hasContent("");
  }

  public FailedTestsToStoreDto prepareDtoAndStoreFirstFailingTests() {
    FailedTestsToStoreDto failedTestsToStore = mock(FailedTestsToStoreDto.class);
    when(failedTestsToStore.getFailedTests())
        .thenReturn(Arrays.asList("my.cool.Test", "my.cool.second.Test"));
    when(failedTestsToStore.getProjectDir()).thenReturn("target");
    TestFailureResultStorage.saveFailedTests(failedTestsToStore);
    return failedTestsToStore;
  }
}
