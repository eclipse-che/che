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

import static org.eclipse.che.plugin.testing.phpunit.server.PHPUnitTestRunner.LOG;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.commons.lang.execution.ProcessHandler;

/**
 * PHPUnit tests running engine.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestEngine {

  private static final String PRINTER_NAME = "ZendPHPUnitLogger";
  private static final String PRINTER_DIRECTORY = "phpunit-printer";
  private static final String PHPUNIT_GLOBAL = "phpunit";
  private static final String PHPUNIT_COMPOSER = "/vendor/bin/phpunit";
  private static final int PRINTER_PORT = 7478;
  private java.nio.file.Path projectsRoot;

  public PHPUnitTestEngine(File projectsRoot) {
    this.projectsRoot = projectsRoot.toPath().normalize().toAbsolutePath();
  }

  public ProcessHandler executeTests(TestExecutionContext context) throws IOException {
    String projectPath = context.getProjectPath();
    String testTargetRelativePath = context.getFilePath();
    String projectAbsolutePath = projectsRoot.resolve(projectPath).toString();
    File testTargetFile = getTestTargetFile(testTargetRelativePath, projectAbsolutePath);
    File testTargetWorkingDirectory =
        testTargetFile.isDirectory() ? testTargetFile : testTargetFile.getParentFile();
    // Get appropriate path to executable
    String phpUnitExecutable = PHPUNIT_GLOBAL;
    if (hasComposerRunner(projectPath)) {
      phpUnitExecutable = projectAbsolutePath + PHPUNIT_COMPOSER;
    }
    // Get appropriate logger for PHP unit version
    final File printerFile = getPrinterFile();
    final String printerDirAbsolutePath = printerFile.getParentFile().getAbsolutePath();
    final CommandLine cmdRunTests =
        new CommandLine(
            phpUnitExecutable,
            "--include-path",
            printerDirAbsolutePath,
            "--printer",
            PRINTER_NAME,
            getTestTarget(testTargetFile));
    ProcessBuilder pb =
        new ProcessBuilder()
            .redirectErrorStream(true)
            .directory(testTargetWorkingDirectory)
            .command(cmdRunTests.toShellCommand());
    pb.environment().put("ZEND_PHPUNIT_PORT", String.valueOf(PRINTER_PORT));
    return new ProcessHandler(pb.start());
  }

  private File getPrinterFile() {
    final String phpLoggerLocation = PRINTER_DIRECTORY + '/' + PRINTER_NAME + ".php";
    final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    final File tmpPrinterFile = new File(tmpDir, phpLoggerLocation);
    if (!tmpPrinterFile.exists()) {
      try {
        tmpPrinterFile.getParentFile().mkdir();
        tmpPrinterFile.createNewFile();
        InputStream printerFileContent =
            getClass().getClassLoader().getResourceAsStream(phpLoggerLocation);
        FileUtils.copyInputStreamToFile(printerFileContent, tmpPrinterFile);
      } catch (Exception e) {
        LOG.error(e.getMessage(), e);
      } finally {
        if (tmpPrinterFile.exists()) {
          tmpPrinterFile.getParentFile().deleteOnExit();
          tmpPrinterFile.deleteOnExit();
        }
      }
    }
    return tmpPrinterFile;
  }

  private File getTestTargetFile(String testTargetRelativePath, String projectAbsolutePath) {
    String[] segments = testTargetRelativePath.split("/");
    if (segments.length > 1) {
      return Paths.get(projectAbsolutePath, segments[1]).toFile();
    }
    return new File(projectAbsolutePath);
  }

  private String getTestTarget(File testTargetFile) {
    if (testTargetFile.isDirectory()) {
      if ((new File(testTargetFile, "phpunit.xml").exists()
          || new File(testTargetFile, "phpunit.xml.dist").exists())) {
        return "";
      }
      return testTargetFile.getAbsolutePath();
    }
    return FilenameUtils.removeExtension(testTargetFile.getAbsolutePath());
  }

  @SuppressWarnings("unchecked")
  private boolean hasComposerRunner(String projectPath) {
    if (!Files.exists(projectsRoot.resolve(projectPath + "/composer.json"))) {
      return false;
    }

    try (InputStream inputStream =
            Files.newInputStream(projectsRoot.resolve(projectPath + "/composer.json"));
        InputStreamReader reader = new InputStreamReader(inputStream)) {
      Gson gson = new GsonBuilder().create();
      Map<String, ?> composerJsonMap = gson.fromJson(reader, LinkedTreeMap.class);
      Map<String, String> requireDev = (Map<String, String>) composerJsonMap.get("require-dev");
      if (requireDev != null && requireDev.get("phpunit/phpunit") != null) {
        return true;
      }
      Map<String, String> require = (Map<String, String>) composerJsonMap.get("require");
      if (require != null && require.get("phpunit/phpunit") != null) {
        return true;
      }
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return false;
  }
}
