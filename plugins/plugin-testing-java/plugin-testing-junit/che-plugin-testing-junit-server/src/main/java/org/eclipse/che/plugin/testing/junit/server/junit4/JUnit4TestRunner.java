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
package org.eclipse.che.plugin.testing.junit.server.junit4;

import com.google.inject.name.Named;
import java.util.ArrayList;
import java.util.List;
import javax.inject.Inject;
import org.eclipse.che.api.testing.shared.TestExecutionContext;
import org.eclipse.che.commons.lang.execution.CommandLine;
import org.eclipse.che.commons.lang.execution.ExecutionException;
import org.eclipse.che.commons.lang.execution.JavaParameters;
import org.eclipse.che.commons.lang.execution.ProcessHandler;
import org.eclipse.che.junit.junit4.CheJUnitCoreRunner;
import org.eclipse.che.plugin.java.languageserver.JavaLanguageServerExtensionService;
import org.eclipse.che.plugin.java.testing.AbstractJavaTestRunner;
import org.eclipse.che.plugin.java.testing.ClasspathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** JUnit implementation for the test runner service. */
public class JUnit4TestRunner extends AbstractJavaTestRunner {
  private static final Logger LOG = LoggerFactory.getLogger(JUnit4TestRunner.class);

  private static final String JUNIT_TEST_NAME = "junit";
  private static final String MAIN_CLASS_NAME = "org.eclipse.che.junit.junit4.CheJUnitLauncher";
  private static final String TEST_METHOD_ANNOTATION = "org.junit.Test";
  private static final String TEST_CLASS_ANNOTATION = "org.junit.runner.RunWith";

  private String workspacePath;

  @Inject
  public JUnit4TestRunner(
      @Named("che.user.workspaces.storage") String workspacePath,
      JavaLanguageServerExtensionService extensionService) {
    super(extensionService, TEST_METHOD_ANNOTATION, TEST_CLASS_ANNOTATION);
    this.workspacePath = workspacePath;
  }

  @Override
  public ProcessHandler execute(TestExecutionContext context) throws ExecutionException {
    return startTestProcess(context);
  }

  @Override
  public String getName() {
    return JUNIT_TEST_NAME;
  }

  private ProcessHandler startTestProcess(TestExecutionContext context) throws ExecutionException {
    JavaParameters parameters = new JavaParameters();
    parameters.setJavaExecutable(System.getProperty("java.home") + "/bin/java");
    parameters.setMainClassName(MAIN_CLASS_NAME);
    parameters.setWorkingDirectory(workspacePath + context.getProjectPath());

    List<String> projectClasspath = getResolvedClassPaths(context);

    List<String> classPath = new ArrayList<>();
    classPath.addAll(projectClasspath);
    classPath.add(ClasspathUtil.getJarPathForClass(CheJUnitCoreRunner.class));
    parameters.getClassPath().addAll(classPath);

    List<String> suite = findTests(context);
    for (String element : suite) {
      parameters.getParametersList().add(element);
    }
    if (context.isDebugModeEnable()) {
      generateDebuggerPort();
      parameters.getVmParameters().add("-Xdebug");
      parameters
          .getVmParameters()
          .add("-Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=" + getDebugPort());
    }
    CommandLine command = parameters.createCommand();
    return new ProcessHandler(command.createProcess());
  }
}
