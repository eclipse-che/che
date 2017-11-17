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
package org.eclipse.che.plugin.testing.testng.server;

import static com.google.common.base.Strings.isNullOrEmpty;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static org.eclipse.che.api.languageserver.service.LanguageServiceUtils.prefixURI;

import com.beust.jcommander.JCommander;
import com.google.inject.name.Named;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.commons.annotation.Nullable;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionManager;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.eclipse.che.plugin.java.testing.JavaTestAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** TestNG implementation for the test runner service. */
public class TestNGRunner extends AbstractJavaTestRunner {
  private static final String TESTNG_NAME = "testng";
  private static final Logger LOG = LoggerFactory.getLogger(TestNGRunner.class);
  private static final String JAVA_IO_TMPDIR = "java.io.tmpdir";
  private static final String TEST_OUTPUT_FOLDER = "/test-output";

  private String workspacePath;
  private JavaLanguageServerExtensionManager extensionManager;
  private final TestNGSuiteUtil suiteUtil;

  @Inject
  public TestNGRunner(
      @Named("che.user.workspaces.storage") String workspacePath,
      JavaLanguageServerExtensionManager extensionManager,
      TestNGSuiteUtil suiteUtil) {
    super(extensionManager);
    this.workspacePath = workspacePath;
    this.extensionManager = extensionManager;
    this.suiteUtil = suiteUtil;
  }

  @Override
  @Nullable
  public ProcessHandler execute(TestExecutionContext context) {
    return startTestProcess(context);
  }

  private ProcessHandler startTestProcess(TestExecutionContext context) {
    File suiteFile = createSuite(context);
    if (suiteFile == null) {
      throw new RuntimeException("Can't create TestNG suite xml file.");
    }

    JavaParameters parameters = new JavaParameters();
    parameters.setJavaExecutable(System.getProperty("java.home") + "/bin/java");
    parameters.setMainClassName("org.testng.CheTestNGLauncher");
    String outputDirectory = getOutputDirectory(context);
    parameters.getParametersList().add("-d", outputDirectory);
    parameters.setWorkingDirectory(workspacePath + context.getProjectPath());
    List<String> classPath = new ArrayList<>();
    List<String> resolvedClassPaths = getResolvedClassPaths(context);
    classPath.addAll(resolvedClassPaths);
    classPath.add(ClasspathUtil.getJarPathForClass(org.testng.CheTestNG.class));
    classPath.add(ClasspathUtil.getJarPathForClass(JCommander.class));
    parameters.getClassPath().addAll(classPath);

    parameters.getParametersList().add("-suiteFile", suiteFile.getAbsolutePath());
    if (context.isDebugModeEnable()) {
      generateDebuggerPort();
      parameters.getVmParameters().add("-Xdebug");
      parameters
          .getVmParameters()
          .add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + getDebugPort());
    }
    CommandLine command = parameters.createCommand();
    try {
      return new ProcessHandler(command.createProcess());
    } catch (ExecutionException e) {
      LOG.error("Can't run TestNG JVM", e);
    }

    return null;
  }

  private String getOutputDirectory(TestExecutionContext context) {
    String outputDir = extensionManager.getOutputDir(prefixURI(context.getProjectPath()));

    if (isNullOrEmpty(outputDir)) {
      return workspacePath + context.getProjectPath() + TEST_OUTPUT_FOLDER;
    } else {
      return outputDir.substring(0, outputDir.lastIndexOf('/')) + TEST_OUTPUT_FOLDER;
    }
  }

  private File createSuite(TestExecutionContext context) {
    String filePath = context.getFilePath();
    if (!isNullOrEmpty(filePath) && filePath.endsWith(".xml")) {
      return suiteUtil.writeSuite(System.getProperty(JAVA_IO_TMPDIR), filePath);
    }
    List<String> testSuite = findTests(context, JavaTestAnnotations.TESTNG_TEST.getName(), "");

    Map<String, List<String>> classes = buildTestNgSuite(testSuite, context);

    return suiteUtil.writeSuite(
        System.getProperty(JAVA_IO_TMPDIR), context.getProjectPath(), classes);
  }

  private Map<String, List<String>> buildTestNgSuite(
      List<String> tests, TestExecutionContext context) {
    switch (context.getContextType()) {
      case FILE:
      case FOLDER:
      case SET:
      case PROJECT:
        return createContainerSuite(tests);
      case CURSOR_POSITION:
        return createMethodSuite(tests);
      default:
        return emptyMap();
    }
  }

  private Map<String, List<String>> createMethodSuite(List<String> tests) {
    if (tests.isEmpty()) {
      return Collections.emptyMap();
    }

    String testMethodDeclaration = tests.get(0);
    int separatorIndex = testMethodDeclaration.indexOf('#');

    if (separatorIndex == -1) {
      return createContainerSuite(tests);
    }

    return Collections.singletonMap(
        testMethodDeclaration.substring(0, separatorIndex),
        singletonList(testMethodDeclaration.substring(separatorIndex + 1)));
  }

  private Map<String, List<String>> createContainerSuite(List<String> tests) {
    if (tests.isEmpty()) {
      return emptyMap();
    }
    Map<String, List<String>> classes = new HashMap<>(tests.size());
    for (String testClass : tests) {
      classes.put(testClass, null);
    }
    return classes;
  }

  /** {@inheritDoc} */
  @Override
  public String getName() {
    return TESTNG_NAME;
  }

  @Override
  protected String getTestAnnotation() {
    return JavaTestAnnotations.TESTNG_TEST.getName();
  }
}
