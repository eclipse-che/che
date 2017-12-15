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

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import org.arquillian.smart.testing.hub.storage.local.TemporaryInternalFiles;
import org.eclipse.che.api.testing.shared.dto.FailedTestsToStoreDto;

public class TestFailureResultStorage {

  public static final String FAILED_TESTS_FILE_NAME = "failedTests";

  static void saveFailedTests(FailedTestsToStoreDto failedTests) {
    StringBuffer sb = new StringBuffer();
    failedTests.getFailedTests().stream().forEach(failedTest -> sb.append(failedTest).append("\n"));
    String pathToProjectDir =
        Paths.get("").toAbsolutePath() + File.separator + failedTests.getProjectDir();

    try {
      new TemporaryInternalFiles()
          .createTestReportDirectoryAction(pathToProjectDir)
          .createWithFile(FAILED_TESTS_FILE_NAME, sb.toString().getBytes());
    } catch (IOException e) {
      throw new UncheckedIOException(
          "Something wrong happened during storing a file with failed tests information", e);
    }
  }
}
