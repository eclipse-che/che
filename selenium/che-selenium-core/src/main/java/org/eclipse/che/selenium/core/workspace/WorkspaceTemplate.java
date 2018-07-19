/*
 * Copyright (c) 2012-2018 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 */
package org.eclipse.che.selenium.core.workspace;

/**
 * Templates file names to create workspaces based upon.
 *
 * @author Dmytro Nochevnov
 */
public class WorkspaceTemplate {
  public static final String ECLIPSE_CPP_GCC = "eclipse_cpp_gcc.json";
  public static final String UBUNTU_LSP = "ubuntu_with_c_sharp_lsp.json";
  public static final String ECLIPSE_NODEJS = "eclipse_nodejs.json";
  public static final String ECLIPSE_PHP = "eclipse_php.json";
  public static final String UBUNTU_JDK8 = "ubuntu_jdk8.json";
  public static final String UBUNTU = "ubuntu.json";
  public static final String DEFAULT = "default.json";
  public static final String DEFAULT_WITH_GITHUB_PROJECTS = "default_with_github_projects.json";
  public static final String PYTHON = "ubuntu_python.json";
  public static final String NODEJS_WITH_JSON_LS = "nodejs_with_json_ls.json";
  public static final String UBUNTU_CAMEL = "ubuntu_jdk8_with_camel_ls.json";
  public static final String ECLIPSE_NODEJS_YAML = "eclipse_nodejs_with_yaml_ls.json";

  private WorkspaceTemplate() {}
}
