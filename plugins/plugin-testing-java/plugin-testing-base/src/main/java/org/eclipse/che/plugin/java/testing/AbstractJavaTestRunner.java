/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.plugin.java.testing;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.languageserver.LanguageServiceUtils.prefixURI;
import static org.eclipse.che.dto.server.DtoFactory.newDto;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import org.eclipse.che.api.testing.server.framework.TestRunner;
import org.eclipse.che.api.testing.shared.TestDetectionContext;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.api.testing.shared.TestPosition;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.TestPositionDto;

/**
 * Abstract java test runner. Can recognize test methods, find java project and compilation unit by
 * path.
 */
public abstract class AbstractJavaTestRunner implements TestRunner {
  private int debugPort = -1;
  private JavaLanguageServerExtensionService extensionService;
  private String testMethodAnnotation;
  private String testClassAnnotation;

  public AbstractJavaTestRunner(
      JavaLanguageServerExtensionService extensionService,
      String testMethodAnnotation,
      String testClassAnnotation) {
    this.extensionService = extensionService;
    this.testMethodAnnotation = testMethodAnnotation;
    this.testClassAnnotation = testClassAnnotation;
  }

  @Override
  public List<TestPosition> detectTests(TestDetectionContext context) {
    List<TestPositionDto> testPositionDtos =
        extensionService.detectTest(
            prefixURI(context.getFilePath()), testMethodAnnotation, context.getOffset());

    return testPositionDtos
        .stream()
        .map(
            it ->
                newDto(TestPosition.class)
                    .withFrameworkName(getName())
                    .withTestName(it.getTestName())
                    .withTestNameLength(it.getTestNameLength())
                    .withTestBodyLength(it.getTestBodyLength())
                    .withTestNameStartOffset(it.getTestNameStartOffset()))
        .collect(Collectors.toList());
  }

  /**
   * Finds tests which should be ran.
   *
   * @param context information about test runner
   * @return list of full qualified names of test classes. If it is the declaration of a test method
   *     it should be: parent fqn + '#' + method name (a.b.c.ClassName#methodName)
   */
  protected List<String> findTests(TestExecutionContext context) {
    return executeFindTestsCommand(context, testMethodAnnotation, testClassAnnotation);
  }

  protected List<String> getResolvedClassPaths(TestExecutionContext context) {
    return extensionService.getResolvedClasspath(prefixURI(context.getProjectPath()));
  }

  private List<String> executeFindTestsCommand(
      TestExecutionContext context, String methodAnnotation, String classAnnotation) {
    switch (context.getContextType()) {
      case PROJECT:
        return extensionService.findTestsFromProject(
            prefixURI(context.getProjectPath()), methodAnnotation, classAnnotation);
      case FILE:
        return extensionService.findTestsInFile(
            prefixURI(context.getFilePath()), methodAnnotation, classAnnotation);
      case FOLDER:
        return extensionService.findTestsFromFolder(
            prefixURI(context.getFilePath()), methodAnnotation, classAnnotation);
      case SET:
        return extensionService.findTestsFromSet(
            methodAnnotation, classAnnotation, context.getListOfTestClasses());
      case CURSOR_POSITION:
        return extensionService.findTestsByCursorPosition(
            prefixURI(context.getFilePath()),
            methodAnnotation,
            classAnnotation,
            context.getCursorOffset());
      default:
        return emptyList();
    }
  }

  @Override
  public int getDebugPort() {
    return debugPort;
  }

  protected void generateDebuggerPort() {
    Random random = new Random();
    int port = random.nextInt(65535);
    if (isPortAvailable(port)) {
      debugPort = port;
    } else {
      generateDebuggerPort();
    }
  }

  private static boolean isPortAvailable(int port) {
    try (Socket ignored = new Socket("localhost", port)) {
      return false;
    } catch (IOException ignored) {
      return true;
    }
  }
}
