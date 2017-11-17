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
package org.eclipse.che.plugin.java.testing;

import static java.util.Collections.emptyList;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;
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
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionManager;
import org.eclipse.che.plugin.java.languageserver.dto.DtoServerImpls.TestPositionDto;

/**
 * Abstract java test runner. Can recognize test methods, find java project and compilation unit by
 * path.
 */
public abstract class AbstractJavaTestRunner implements TestRunner {
  private int debugPort = -1;
  private JavaLanguageServerExtensionManager extensionManager;

  public AbstractJavaTestRunner(JavaLanguageServerExtensionManager extensionManager) {
    this.extensionManager = extensionManager;
  }

  @Override
  public List<TestPosition> detectTests(TestDetectionContext context) {
    List<TestPositionDto> testPositionDtos =
        extensionManager.detectTest(
            prefixURI(context.getFilePath()), getTestAnnotation(), context.getOffset());

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

  /** Returns import of test annotation */
  protected abstract String getTestAnnotation();

  /**
   * Finds tests which should be ran.
   *
   * @param context information about test runner
   * @param methodAnnotation java annotation which describes test method in the test framework
   * @param classAnnotation java annotation which describes test class in the test framework
   * @return list of full qualified names of test classes. If it is the declaration of a test method
   *     it should be: parent fqn + '#' + method name (a.b.c.ClassName#methodName)
   */
  protected List<String> findTests(
      TestExecutionContext context, String methodAnnotation, String classAnnotation) {
    return executeFindTestsCommand(context, methodAnnotation, classAnnotation);
  }

  protected List<String> getResolvedClassPaths(TestExecutionContext context) {
    return extensionManager.getResolvedClasspath(prefixURI(context.getProjectPath()));
  }

  private List<String> executeFindTestsCommand(
      TestExecutionContext context, String methodAnnotation, String classAnnotation) {
    switch (context.getContextType()) {
      case PROJECT:
        return extensionManager.findTestsFromProject(
            prefixURI(context.getProjectPath()), methodAnnotation, classAnnotation);
      case FILE:
        return extensionManager.findTestsInFile(
            prefixURI(context.getFilePath()), methodAnnotation, classAnnotation);
      case FOLDER:
        return extensionManager.findTestsFromFolder(
            prefixURI(context.getFilePath()), methodAnnotation, classAnnotation);
      case SET:
        return extensionManager.findTestsFromSet(
            methodAnnotation, classAnnotation, context.getListOfTestClasses());
      case CURSOR_POSITION:
        return extensionManager.findTestsByCursorPosition(
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
