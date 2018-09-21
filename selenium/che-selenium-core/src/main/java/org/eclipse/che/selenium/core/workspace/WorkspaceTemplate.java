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
package org.eclipse.che.selenium.core.workspace;

/**
 * Templates file names to create workspaces based upon.
 *
 * @author Dmytro Nochevnov
 */
public enum WorkspaceTemplate {
  BROKEN("broken_workspace.json"),
  DEFAULT("default.json"),
  DEFAULT_WITH_GITHUB_PROJECTS("default_with_github_projects.json"),
  ECLIPSE_PHP("eclipse_php.json"),
  ECLIPSE_NODEJS("eclipse_nodejs.json"),
  ECLIPSE_CPP_GCC("eclipse_cpp_gcc.json"),
  ECLIPSE_NODEJS_YAML("eclipse_nodejs_with_yaml_ls.json"),
  PYTHON("ubuntu_python.json"),
  NODEJS_WITH_JSON_LS("nodejs_with_json_ls.json"),
  UBUNTU("ubuntu.json"),
  UBUNTU_GO("ubuntu_go.json"),
  UBUNTU_JDK8("ubuntu_jdk8.json"),
  UBUNTU_LSP("ubuntu_with_c_sharp_lsp.json"),
  UBUNTU_CAMEL("ubuntu_jdk8_with_camel_ls.json");

  private final String templateFileName;

  WorkspaceTemplate(String templateFileName) {
    this.templateFileName = templateFileName;
  }

  public String getTemplateFileName() {
    return templateFileName;
  }
}
