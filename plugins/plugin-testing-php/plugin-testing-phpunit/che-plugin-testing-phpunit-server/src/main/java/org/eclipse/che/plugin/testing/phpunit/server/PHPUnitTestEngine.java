/**
 * ***************************************************************************** Copyright (c) 2016
 * Rogue Wave Software, Inc. All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which accompanies this
 * distribution, and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * <p>Contributors: Rogue Wave Software, Inc. - initial API and implementation
 * *****************************************************************************
 */
package org.eclipse.che.plugin.testing.phpunit.server;

import static org.eclipse.che.plugin.testing.phpunit.server.PHPUnitTestRunner.LOG;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.che.api.core.util.AbstractLineConsumer;
import org.eclipse.che.api.core.util.CommandLine;
import org.eclipse.che.api.core.util.ProcessUtil;
import org.eclipse.che.api.testing.server.exceptions.TestFrameworkException;
import org.eclipse.che.api.testing.shared.dto.TestResultDto;
import org.eclipse.che.api.testing.shared.dto.TestResultRootDto;
import org.eclipse.che.api.vfs.Path;
import org.eclipse.che.plugin.testing.phpunit.server.model.PHPUnitTestRoot;

/**
 * PHPUnit tests running engine.
 *
 * @author Bartlomiej Laczkowski
 */
public class PHPUnitTestEngine {

  private java.nio.file.Path projectsRoot;

  private final class PrinterListener implements Runnable {

    private ServerSocket serverSocket;
    private Socket socket;
    private Gson gson = new GsonBuilder().create();
    private ExecutorService threadExecutor;

    public PrinterListener() {
      threadExecutor =
          Executors.newSingleThreadExecutor(
              new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                  final Thread thread = new Thread(r, "PHPUnitPrinterListener");
                  thread.setDaemon(true);
                  return thread;
                }
              });
    }

    @Override
    public void run() {
      try {
        serverSocket = new ServerSocket(PRINTER_PORT, 1);
        serverSocket.setSoTimeout(3000);
        serverSocket.setReuseAddress(true);
        // Release engine to perform tests
        latchReady.countDown();
        socket = serverSocket.accept();
        handleReport(socket);
      } catch (final IOException e) {
        Thread.currentThread().interrupt();
      } finally {
        shutdown();
      }
    }

    void shutdown() {
      try {
        if (socket != null && !socket.isClosed()) socket.close();
      } catch (final Exception e) {
      }
      try {
        if (serverSocket != null && !serverSocket.isClosed()) serverSocket.close();
      } catch (final IOException e) {
      }
      threadExecutor.shutdown();
    }

    void startup() {
      threadExecutor.submit(this);
    }

    @SuppressWarnings("unchecked")
    private void handleReport(final Socket socket) {
      try {
        final BufferedReader reader =
            new BufferedReader(new InputStreamReader(socket.getInputStream()));
        final PHPUnitMessageParser messageParser = new PHPUnitMessageParser(phpTestsRoot);
        String line;
        Map<String, String> value = null;
        while ((line = reader.readLine()) != null) {
          try {
            value = gson.fromJson(line, LinkedTreeMap.class);
            messageParser.parse(value);
          } catch (final Throwable e) {
            value = null;
          }
        }
        latchDone.countDown();
        shutdown();
      } catch (final IOException e) {
        Thread.currentThread().interrupt();
        shutdown();
      }
    }
  }

  private static final String PRINTER_NAME = "ZendPHPUnitLogger";
  private static final String PRINTER_DIRECTORY = "phpunit-printer";
  private static final String PHPUNIT_GLOBAL = "phpunit";
  private static final String PHPUNIT_COMPOSER = "/vendor/bin/phpunit";
  private static final int PRINTER_PORT = 7478;

  private final CountDownLatch latchReady = new CountDownLatch(1);
  private final CountDownLatch latchDone = new CountDownLatch(1);

  private PHPUnitTestRoot phpTestsRoot;
  private PHPUnitTestResultsProvider testResultsProvider;

  public PHPUnitTestEngine(File projectsRoot) {
    this.projectsRoot = projectsRoot.toPath().normalize().toAbsolutePath();
  }

  /**
   * Executes PHP unit tests with the use of provided parameters.
   *
   * @param testParameters
   * @return
   * @throws Exception
   */
  public TestResultRootDto executeTests(Map<String, String> testParameters) throws Exception {
    String projectPath = testParameters.get("projectPath");
    String projectAbsolutePath = testParameters.get("absoluteProjectPath");
    String testTargetRelativePath = testParameters.get("testTarget");
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
    PrinterListener printerListener = new PrinterListener();
    printerListener.startup();
    // Reset provider & tests root
    testResultsProvider = new PHPUnitTestResultsProvider();
    phpTestsRoot = new PHPUnitTestRoot();
    // Wait for listener thread to be started
    try {
      latchReady.await();
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
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
    Process processRunPHPUnitTests = pb.start();
    final StringBuilder stdErrOut = new StringBuilder();
    ProcessUtil.process(
        processRunPHPUnitTests,
        new AbstractLineConsumer() {
          @Override
          public void writeLine(String line) throws IOException {
            if (!line.isEmpty()) stdErrOut.append(line + "\n");
          }
        });
    int exitValue = processRunPHPUnitTests.waitFor();
    try {
      latchDone.await();
    } catch (InterruptedException e) {
      LOG.error(e.getMessage(), e);
    }
    if (exitValue != 0 && stdErrOut.length() > 0 && phpTestsRoot.getChildren() == null) {
      throw new TestFrameworkException("PHPUnit Error:\n" + stdErrOut.toString());
    }
    return testResultsProvider.getTestResultsRoot(phpTestsRoot);
  }

  /**
   * Returns test results for given result path.
   *
   * @param testResultsPath
   * @return test results for given result path
   */
  public List<TestResultDto> getTestResults(List<String> testResultsPath) {
    return testResultsProvider.getTestResults(testResultsPath);
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
    if (Path.of(testTargetRelativePath).length() > 1)
      return new File(
          Path.of(projectAbsolutePath)
              .newPath(Path.of(testTargetRelativePath).subPath(1))
              .toString());
    return new File(Path.of(projectAbsolutePath).toString());
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
      if (requireDev != null && requireDev.get("phpunit/phpunit") != null) return true;
      Map<String, String> require = (Map<String, String>) composerJsonMap.get("require");
      if (require != null && require.get("phpunit/phpunit") != null) return true;
    } catch (Exception e) {
      LOG.error(e.getMessage(), e);
    }
    return false;
  }
}
