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
package org.eclipse.che.commons.lang.execution;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all parameters that needed to start new JVM instance. <br>
 * Set all needed parameters and call {@link #createCommand()} to receive command line.
 *
 * @author Evgen Vidolob
 */
public class JavaParameters {

  private String workingDirectory;
  private String javaExecutable;
  private String jarPath;
  private String mainClassName;

  private List<String> classPath = new ArrayList<>();
  private List<String> vmParameters = new ArrayList<>();
  private Map<String, String> enviroment = new HashMap<>();

  private ParametersList parametersList = new ParametersList();

  public String getJavaExecutable() {
    return javaExecutable;
  }

  public String getMainClassName() {
    return mainClassName;
  }

  public List<String> getClassPath() {
    return classPath;
  }

  public List<String> getVmParameters() {
    return vmParameters;
  }

  public String getWorkingDirectory() {
    return workingDirectory;
  }

  public ParametersList getParametersList() {
    return parametersList;
  }

  public void setWorkingDirectory(String workingDirectory) {
    this.workingDirectory = workingDirectory;
  }

  public void setJavaExecutable(String javaExecutable) {
    this.javaExecutable = javaExecutable;
  }

  public void setMainClassName(String mainClassName) {
    this.mainClassName = mainClassName;
  }

  public String getJarPath() {
    return jarPath;
  }

  public void setJarPath(String jarPath) {
    this.jarPath = jarPath;
  }

  public Map<String, String> getEnviroment() {
    return enviroment;
  }

  public CommandLine createCommand() {
    CommandLine result = new CommandLine(javaExecutable);
    result.getEnvironment().putAll(enviroment);
    result.addParameters(vmParameters);
    result.addParameter("-classpath");
    result.addParameter(createClasspath(classPath));

    if (mainClassName != null) {
      result.addParameter(mainClassName);
    } else if (jarPath != null) {
      result.addParameter("-jar");
      result.addParameter(jarPath);
    }

    result.addParameters(parametersList.getParameters());

    result.setWorkingDirectory(workingDirectory);
    return result;
  }

  private String createClasspath(List<String> classPath) {
    return classPath.stream().reduce((s, s2) -> s + ":" + s2).get();
  }
}
